package com.umar.carrental.controller;

import com.umar.carrental.dto.BookingRequest;
import com.umar.carrental.dto.BookingResponse;
import com.umar.carrental.model.*;
import com.umar.carrental.repository.BookingRepository;
import com.umar.carrental.repository.CarRepository;
import com.umar.carrental.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.umar.carrental.dto.BookedRange;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public BookingController(BookingRepository bookingRepository,
                             CarRepository carRepository,
                             UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public BookingResponse book(@Valid @RequestBody BookingRequest req, Authentication auth) {
        if (req.startDate().isBefore(LocalDate.now()) || req.endDate().isBefore(req.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid dates");
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Car car = carRepository.findById(req.carId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));

        if (!Boolean.TRUE.equals(car.getAvailable())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Car is not available");
        }

        boolean overlap = bookingRepository
                .existsByCarIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        car.getId(), BookingStatus.CONFIRMED, req.endDate(), req.startDate());
        if (overlap) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Car already booked for these dates");
        }

        long days = ChronoUnit.DAYS.between(req.startDate(), req.endDate()) + 1;

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCar(car);
        booking.setStartDate(req.startDate());
        booking.setEndDate(req.endDate());
        booking.setTotalPrice(days * car.getPricePerDay());

        return BookingResponse.from(bookingRepository.save(booking));
    }

    @GetMapping("/my")
    public List<BookingResponse> myBookings(Authentication auth) {
        return bookingRepository.findByUserEmail(auth.getName())
                .stream().map(BookingResponse::from).toList();
    }

    @GetMapping
    public List<BookingResponse> allBookings() {
        return bookingRepository.findAll()
                .stream().map(BookingResponse::from).toList();
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id, Authentication auth) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = booking.getUser().getEmail().equals(auth.getName());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(bookingRepository.save(booking));
    }
    @GetMapping("/car/{carId}/booked")
    public List<BookedRange> bookedRanges(@PathVariable Long carId) {
        return bookingRepository.findByCarIdAndStatus(carId, BookingStatus.CONFIRMED)
                .stream()
                .map(b -> new BookedRange(b.getStartDate(), b.getEndDate()))
                .toList();
    }
}