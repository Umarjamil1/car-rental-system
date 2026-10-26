package com.umar.carrental.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull Long carId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}