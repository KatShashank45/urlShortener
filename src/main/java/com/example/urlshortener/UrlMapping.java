package com.example.urlshortener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

// @Entity tells JPA that this class is an entity and should be mapped to a database table.
@Entity
// @Table(name = "url_mappings") explicitly specifies the table name.
// If omitted, JPA might use "urlmapping" or "UrlMapping" based on the class name.
@Table(name = "url_mappings")
public class UrlMapping {

    // @Id marks this field as the primary key for the table.
    @Id
    // @GeneratedValue specifies how the primary key is generated.
    // GenerationType.IDENTITY typically means the database will auto-increment it.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(unique = true, nullable = false, length = 7)
    // unique=true: ensures each short_code is unique in the database.
    // nullable=false: means this field cannot be empty.
    // length=7: sets the column length for the short code.
    @Column(name = "short_code", unique = true, nullable = false, length = 7)
    private String shortCode;

    // @Column(nullable = false, columnDefinition = "TEXT")
    // columnDefinition="TEXT": allows for very long URLs. Standard VARCHAR might be too short.
    @Column(name = "long_url", nullable = false, columnDefinition = "TEXT")
    private String longUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    // JPA requires a no-argument constructor.
    public UrlMapping() {
    }

    public UrlMapping(String shortCode, String longUrl) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.createdAt = LocalDateTime.now(); // Set creation time automatically
    }

    // Getters and Setters
    // JPA uses these to access the fields.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}