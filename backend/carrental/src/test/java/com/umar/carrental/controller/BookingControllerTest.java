package com.umar.carrental.controller;

import com.umar.carrental.dto.BookingRequest;
import com.umar.carrental.dto.BookingResponse;
import com.umar.carrental.model.Booking;
import com.umar.carrental.model.BookingStatus;
import com.umar.carrental.model.Car;
import com.umar.carrental.model.User;
import com.umar.carrental.repository.BookingRepository;
import com.umar.carrental.repository.CarRepository;
import com.umar.carrental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingControllerTest {

    private BookingRepository bookingRepository;
    private CarRepository carRepository;
    private UserRepository userRepository;
    private BookingController controller;
    private Car car;
    private User user;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        carRepository = mock(CarRepository.class);
        userRepository = mock(UserRepository.class);
        controller = new BookingController(bookingRepository, carRepository, userRepository);

        car = new Car();
        car.setId(7L);
        car.setBrand("Toyota");
        car.setModel("Corolla");
        car.setPricePerDay(9000.0);
        car.setAvailable(true);

        user = new User();
        user.setEmail("umar@test.com");

        when(userRepository.findByEmail("umar@test.com")).thenReturn(Optional.of(user));
        when(carRepository.findById(7L)).thenReturn(Optional.of(car));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Authentication auth(String email, String role) {
        return new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void bookingThreeDaysCalculatesCorrectTotal() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(2);

        BookingResponse res = controller.book(
                new BookingRequest(7L, start, end), auth("umar@test.com", "USER"));

        assertEquals(27000.0, res.totalPrice());
        assertEquals("CONFIRMED", res.status());
    }

    @Test
    void endDateBeforeStartDateGives400() {
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = start.minusDays(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.book(new BookingRequest(7L, start, end), auth("umar@test.com", "USER")));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void unavailableCarGives409() {
        car.setAvailable(false);
        LocalDate start = LocalDate.now().plusDays(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.book(new BookingRequest(7L, start, start.plusDays(1)), auth("umar@test.com", "USER")));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void overlappingDatesGive409() {
        when(bookingRepository
                .existsByCarIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        anyLong(), any(), any(), any()))
                .thenReturn(true);
        LocalDate start = LocalDate.now().plusDays(1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.book(new BookingRequest(7L, start, start.plusDays(1)), auth("umar@test.com", "USER")));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void anotherUserCannotCancelMyBooking() {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCar(car);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.cancel(1L, auth("other@test.com", "USER")));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void ownerCanCancelOwnBooking() {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCar(car);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponse res = controller.cancel(1L, auth("umar@test.com", "USER"));

        assertEquals(BookingStatus.CANCELLED.name(), res.status());
    }
}