package com.ridelink.ride.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ride.dto.RideRequestDto;
import com.ridelink.ride.dto.RideResponseDto;
import com.ridelink.ride.model.Location;
import com.ridelink.ride.model.RideStatus;
import com.ridelink.ride.security.JwtUtil;
import com.ridelink.ride.service.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RideControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    
    @MockBean private RideService rideService;
    @MockBean private JwtUtil jwtUtil;
    
    @Test
    @WithMockUser(roles = "PASSENGER")
    void requestRide_ShouldReturn201() throws Exception {
        RideRequestDto request = new RideRequestDto();
        request.setPickupLocation(Location.builder().placeName("A").build());
        request.setDestinationLocation(Location.builder().placeName("B").build());
        
        RideResponseDto response = RideResponseDto.builder()
            .id("ride-123")
            .status(RideStatus.REQUESTED)
            .passengerId("passenger-1")
            .build();
        
        when(rideService.requestRide(any(), anyString())).thenReturn(response);
        
        mockMvc.perform(post("/api/v1/rides")
                .with(user("passenger-1").roles("PASSENGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("ride-123"))
            .andExpect(jsonPath("$.status").value("REQUESTED"));
    }
    
    @Test
    void requestRide_ShouldReturn401_WhenNoAuth() throws Exception {
        mockMvc.perform(post("/api/v1/rides")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }
}