package com.umar.carrental.repository;

import com.umar.carrental.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    @Query("""
    SELECT c FROM Car c
    WHERE c.available = true
    AND c.id NOT IN (
        SELECT b.car.id FROM Booking b
        WHERE b.status = com.umar.carrental.model.BookingStatus.CONFIRMED
        AND b.startDate <= :end AND b.endDate >= :start)
    """)
    List<Car> findAvailable(@Param("start") LocalDate start, @Param("end") LocalDate end);
}