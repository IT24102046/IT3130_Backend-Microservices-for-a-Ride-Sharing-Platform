package com.ridelink.driver_service.service;

import com.ridelink.driver_service.dto.DriverRegistrationRequest;
import com.ridelink.driver_service.dto.DriverResponse;
import com.ridelink.driver_service.exception.BusinessException;
import com.ridelink.driver_service.exception.ResourceNotFoundException;
import com.ridelink.driver_service.model.Driver;
import com.ridelink.driver_service.model.DriverStatus;
import com.ridelink.driver_service.repository.DriverRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

class DriverServiceTest {

    private DriverRepository driverRepository;

    private DriverService driverService;

    private boolean accountExists;
    private String requestedId;
    private String requestedArea;

    private DriverRegistrationRequest validRequest;
    private Driver savedDriver;

    void setUp() {
        driverRepository = (DriverRepository) Proxy.newProxyInstance(
            DriverRepository.class.getClassLoader(),
            new Class<?>[]{DriverRepository.class},
            (proxy, method, args) -> {
                return switch (method.getName()) {
                    case "existsByAccountId" -> accountExists;
                    case "save" -> savedDriver;
                    case "findById" -> Optional.ofNullable(
                        "missing".equals(requestedId) ? null : savedDriver);
                    case "findByStatusAndServiceAreasContaining" ->
                        List.of(savedDriver);
                    default -> method.getReturnType().equals(boolean.class) ? false : null;
                };
            });
        driverService = new DriverService(driverRepository);

        validRequest = new DriverRegistrationRequest();
        validRequest.setAccountId("acc-123");
        validRequest.setFullName("John Doe");
        validRequest.setEmail("john@example.com");
        validRequest.setPhone("+94771234567");
        validRequest.setLicenseNumber("LIC-001");
        validRequest.setServiceAreas(List.of("Colombo", "Kandy"));

        savedDriver = Driver.builder()
            .id("drv-1")
            .accountId("acc-123")
            .fullName("John Doe")
            .email("john@example.com")
            .phone("+94771234567")
            .licenseNumber("LIC-001")
            .serviceAreas(List.of("Colombo", "Kandy"))
            .status(DriverStatus.OFFLINE)
            .rating(5.0)
            .totalRides(0)
            .build();
    }

    void registerDriver_ShouldSucceed_WhenAccountIsNew() {
        accountExists = false;

        DriverResponse response = driverService.registerDriver(validRequest);

        check(response != null, "response must not be null");
        check("drv-1".equals(response.getId()), "unexpected driver id");
        check("John Doe".equals(response.getFullName()), "unexpected driver name");
        check(DriverStatus.OFFLINE == response.getStatus(), "unexpected driver status");
    }

    void registerDriver_ShouldThrow_WhenAccountExists() {
        accountExists = true;

        BusinessException ex = expectThrows(BusinessException.class,
            () -> driverService.registerDriver(validRequest));

        check("Driver already registered for this account".equals(ex.getMessage()),
            "unexpected exception message");
    }

    void getDriverById_ShouldThrow_WhenNotFound() {
        requestedId = "missing";

        expectThrows(ResourceNotFoundException.class,
            () -> driverService.getDriverById("missing"));
    }

    void getEligibleDrivers_ShouldFilterByArea_WhenAreaProvided() {
        requestedArea = "Colombo";

        List<DriverResponse> result = driverService.getEligibleDrivers("Colombo");

        check(result.size() == 1, "unexpected eligible driver count");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static <T extends Throwable> T expectThrows(Class<T> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable exception) {
            if (type.isInstance(exception)) {
                return type.cast(exception);
            }
            throw new AssertionError("Unexpected exception type", exception);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
}