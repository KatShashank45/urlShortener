package com.example.urlshortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class UrlShortenerController {

    // Using a simple counter for ID generation before Base62 encoding.
    // For a production system, a more robust distributed ID generator would be needed,
    // or rely on a database sequence that Base62Utils directly converts.
    // For now, we ensure the shortCode is unique via DB constraint.
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis()); // Initialize with current time for more variability

    // @Autowired tells Spring to "inject" an instance of UrlMappingRepository here.
    // This is called Dependency Injection. Spring manages creating the repository object for us.
    private final UrlMappingRepository urlMappingRepository;

    @Autowired
    public UrlShortenerController(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @PostMapping("/api/v1/shorten") // Changed path slightly for clarity
    public ResponseEntity<String> shortenUrl(@RequestBody String longUrl) {
        // Basic validation for the long URL (can be improved)
        if (longUrl == null || longUrl.trim().isEmpty() || !longUrl.startsWith("http")) {
            return ResponseEntity.badRequest().body("Invalid URL provided.");
        }

        String shortCode;
        boolean isUnique = false;
        int attempts = 0;
        final int MAX_ATTEMPTS = 5; // Max attempts to find a unique short code

        // Loop to ensure shortCode uniqueness, though with Base62 from a counter,
        // collisions are unlikely until the counter wraps or if we switch to random generation.
        // The DB unique constraint on short_code is the ultimate guard.
        do {
            long uniqueId = counter.getAndIncrement();
            shortCode = Base62Utils.toBase62(uniqueId);
            // Ensure shortCode is 7 characters, pad if necessary (unlikely with current counter start)
            while(shortCode.length() < 7) {
                // This padding strategy is very basic. A better way is to ensure
                // the initial number for Base62 is large enough.
                // Our counter starting with System.currentTimeMillis() should give > 7 chars.
                // Or, use a fixed-size encoding if possible.
                // For Base62, 62^6 < ID < 62^7 will give 7 chars.
                // The current counter will likely produce more than 7 chars. Let's truncate or adjust.
                // For simplicity, let's assume our Base62 will be long enough and we might truncate
                // or we adjust the counter's initial value for a 7-char target.
                // For now, if it's too short, we'll just regenerate (which the loop does).
                // Let's ensure it's not longer than 7 for now.
                uniqueId = counter.getAndIncrement(); // Get a new ID if padding was needed.
                shortCode = Base62Utils.toBase62(uniqueId);
            }

            if (shortCode.length() > 7) {
                shortCode = shortCode.substring(0, 7); // Truncate to 7 characters
            }

            if (urlMappingRepository.findByShortCode(shortCode).isEmpty()) {
                isUnique = true;
            }
            attempts++;
        } while (!isUnique && attempts < MAX_ATTEMPTS);

        if (!isUnique) {
            // Could not generate a unique short code after several attempts
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not generate a unique short code.");
        }

        UrlMapping newMapping = new UrlMapping(shortCode, longUrl.trim());
        newMapping.setCreatedAt(LocalDateTime.now()); // Explicitly set creation time

        try {
            urlMappingRepository.save(newMapping);
            // Construct the full short URL to return to the user
            // Assuming app runs on localhost:8080 for now.
            // Later, this should come from configuration.
            String fullShortUrl = "http://localhost:8080/" + shortCode;
            return ResponseEntity.ok("Short URL created: " + fullShortUrl);
        } catch (Exception e) {
            // This might catch DataIntegrityViolationException if short_code somehow wasn't unique
            // despite our check (e.g., race condition if not using DB sequence for ID generation directly)
            System.err.println("Error saving URL mapping: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating short URL. It might already exist or there was a database issue.");
        }
    }

    // @GetMapping("/{shortCode}") tells Spring this method handles HTTP GET requests
    // where {shortCode} is a path variable (part of the URL).
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortCode) {
        // @PathVariable String shortCode tells Spring to take the value from the URL path
        // (e.g., if URL is /XyZ123a, shortCode will be "XyZ123a")
        // and put it into the 'shortCode' variable.

        Optional<UrlMapping> urlMappingOptional = urlMappingRepository.findByShortCode(shortCode);

        if (urlMappingOptional.isPresent()) {
            UrlMapping mapping = urlMappingOptional.get();
            HttpHeaders headers = new HttpHeaders();
            try {
                headers.setLocation(URI.create(mapping.getLongUrl()));
            } catch (IllegalArgumentException e) {
                // Handle invalid URI stored in DB, perhaps return an error
                System.err.println("Invalid URI syntax for long URL: " + mapping.getLongUrl());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            // HttpStatus.FOUND is HTTP status code 302, used for redirection.
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } else {
            // If the shortCode is not found in the database, return HTTP 404 Not Found.
            return ResponseEntity.notFound().build();
        }
    }
}