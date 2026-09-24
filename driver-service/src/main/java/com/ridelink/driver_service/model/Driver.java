package com.ridelink.driver_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;

    @Indexed(unique = true)
    private String accountId;

    private String fullName;
    private String email;
    private String phone;
    private String licenseNumber;
    private String vehicleId;

    @Indexed
    private DriverStatus status;

    private List<String> serviceAreas;

    @GeoSpatialIndexed
    private GeoJsonPoint currentLocation;

    private Double rating;
    private Integer totalRides;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActiveAt;

    // Default constructor
    public Driver() {}

    // All-args constructor
    public Driver(String id, String accountId, String fullName, String email,
                  String phone, String licenseNumber, String vehicleId,
                  DriverStatus status, List<String> serviceAreas,
                  GeoJsonPoint currentLocation, Double rating, Integer totalRides,
                  LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastActiveAt) {
        this.id = id;
        this.accountId = accountId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.vehicleId = vehicleId;
        this.status = status;
        this.serviceAreas = serviceAreas;
        this.currentLocation = currentLocation;
        this.rating = rating;
        this.totalRides = totalRides;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastActiveAt = lastActiveAt;
    }

    // Getters
    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getVehicleId() { return vehicleId; }
    public DriverStatus getStatus() { return status; }
    public List<String> getServiceAreas() { return serviceAreas; }
    public GeoJsonPoint getCurrentLocation() { return currentLocation; }
    public Double getRating() { return rating; }
    public Integer getTotalRides() { return totalRides; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public void setStatus(DriverStatus status) { this.status = status; }
    public void setServiceAreas(List<String> serviceAreas) { this.serviceAreas = serviceAreas; }
    public void setCurrentLocation(GeoJsonPoint currentLocation) { this.currentLocation = currentLocation; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setTotalRides(Integer totalRides) { this.totalRides = totalRides; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    // Builder (manual)
    public static DriverBuilder builder() { return new DriverBuilder(); }

    public static class DriverBuilder {
        private Driver driver = new Driver();

        public DriverBuilder id(String id) { driver.setId(id); return this; }
        public DriverBuilder accountId(String accountId) { driver.setAccountId(accountId); return this; }
        public DriverBuilder fullName(String fullName) { driver.setFullName(fullName); return this; }
        public DriverBuilder email(String email) { driver.setEmail(email); return this; }
        public DriverBuilder phone(String phone) { driver.setPhone(phone); return this; }
        public DriverBuilder licenseNumber(String licenseNumber) { driver.setLicenseNumber(licenseNumber); return this; }
        public DriverBuilder vehicleId(String vehicleId) { driver.setVehicleId(vehicleId); return this; }
        public DriverBuilder status(DriverStatus status) { driver.setStatus(status); return this; }
        public DriverBuilder serviceAreas(List<String> serviceAreas) { driver.setServiceAreas(serviceAreas); return this; }
        public DriverBuilder currentLocation(GeoJsonPoint currentLocation) { driver.setCurrentLocation(currentLocation); return this; }
        public DriverBuilder rating(Double rating) { driver.setRating(rating); return this; }
        public DriverBuilder totalRides(Integer totalRides) { driver.setTotalRides(totalRides); return this; }
        public DriverBuilder createdAt(LocalDateTime createdAt) { driver.setCreatedAt(createdAt); return this; }
        public DriverBuilder updatedAt(LocalDateTime updatedAt) { driver.setUpdatedAt(updatedAt); return this; }
        public DriverBuilder lastActiveAt(LocalDateTime lastActiveAt) { driver.setLastActiveAt(lastActiveAt); return this; }

        public Driver build() { return driver; }
    }
}