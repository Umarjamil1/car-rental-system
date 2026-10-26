package com.umar.carrental.dto;

import com.umar.carrental.model.Booking;

import java.time.LocalDate;

public record BookingResponse(
        Long id,
        Long carId,
        String car,
        String userEmail,
        LocalDate startDate,
        LocalDate endDate,
        Double totalPrice,
        String status
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getCar().getId(),
                b.getCar().getBrand() + " " + b.getCar().getModel(),
                b.getUser().getEmail(),
                b.getStartDate(),
                b.getEndDate(),
                b.getTotalPrice(),
                b.getStatus().name());
    }
}