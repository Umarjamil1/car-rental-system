package com.umar.carrental.repository;

import com.umar.carrental.model.Booking;
import com.umar.carrental.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserEmail(String email);
    List<Booking> findByCarIdAndStatus(Long carId, BookingStatus status);


    boolean existsByCarIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long carId, BookingStatus status, LocalDate end, LocalDate start);
}
