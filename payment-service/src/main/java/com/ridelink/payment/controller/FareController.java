package com.ridelink.payment.controller;

import com.ridelink.payment.dto.ApiErrorResponse;
import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.dto.FinalFareRequest;
import com.ridelink.payment.dto.FinalFareResponse;
import com.ridelink.payment.service.FareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping(value = "/estimate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Estimate a ride fare",
            description = "Calculates an estimate using LKR 200.00 base fare plus LKR 75.00 per kilometre."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = FareEstimateRequest.class),
                    examples = @ExampleObject(value = "{\"distanceKm\": 10.0}")
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Fare estimate calculated successfully",
                    content = @Content(schema = @Schema(implementation = FareEstimateResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed because distanceKm is missing, null, zero, or negative",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-09-22T18:30:00+05:30",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Validation failed",
                                      "path": "/api/fares/estimate",
                                      "fieldErrors": {
                                        "distanceKm": "Distance must be greater than 0"
                                      }
                                    }
                                    """)
                    )
            )
    })
    public FareEstimateResponse estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        return fareService.estimateFare(request.distanceKm());
    }

    @PostMapping(value = "/final", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Calculate the final fare for a completed ride",
            description = "Calculates the final fare from supplied completed-distance data using LKR 200.00 base fare "
                    + "plus LKR 75.00 per kilometre. Ride Service integration will be added later."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    schema = @Schema(implementation = FinalFareRequest.class),
                    examples = @ExampleObject(value = """
                            {
                              "rideId": "ride-7f3a",
                              "passengerId": "passenger-42",
                              "distanceKm": 10.0
                            }
                            """)
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Final fare calculated successfully",
                    content = @Content(schema = @Schema(implementation = FinalFareResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed because an identifier or distance is invalid",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-09-22T18:30:00+05:30",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Validation failed",
                                      "path": "/api/fares/final",
                                      "fieldErrors": {
                                        "rideId": "Ride ID is required"
                                      }
                                    }
                                    """)
                    )
            )
    })
    public FinalFareResponse calculateFinalFare(@Valid @RequestBody FinalFareRequest request) {
        return fareService.calculateFinalFare(request.rideId(), request.passengerId(), request.distanceKm());
    }
}
