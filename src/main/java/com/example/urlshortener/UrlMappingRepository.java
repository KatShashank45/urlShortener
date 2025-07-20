package com.example.urlshortener;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// @Repository tells Spring that this is a repository bean.
// For JpaRepository, this annotation is technically optional because Spring Boot
// can detect it, but it's good practice to include it.
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    // Spring Data JPA will automatically understand this method name.
    // It will generate a query to find a UrlMapping by its shortCode field.
    // Optional<UrlMapping> means it might find a UrlMapping, or it might not (if the shortCode doesn't exist).
    Optional<UrlMapping> findByShortCode(String shortCode);

}