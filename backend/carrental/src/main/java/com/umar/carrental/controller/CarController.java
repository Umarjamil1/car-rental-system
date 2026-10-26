package com.umar.carrental.controller;

import com.umar.carrental.model.Car;
import com.umar.carrental.repository.CarRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarRepository carRepository;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @GetMapping
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    @PostMapping
    public Car addCar(@RequestBody Car car) {
        return carRepository.save(car);
    }

    @GetMapping("/{id}")
    public Car getCar(@PathVariable Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
    }

    @PutMapping("/{id}")
    public Car updateCar(@PathVariable Long id, @RequestBody Car updated) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Car not found"));
        car.setBrand(updated.getBrand());
        car.setModel(updated.getModel());
        car.setYear(updated.getYear());
        car.setPricePerDay(updated.getPricePerDay());
        car.setAvailable(updated.getAvailable());
        return carRepository.save(car);
    }

    @DeleteMapping("/{id}")
    public String deleteCar(@PathVariable Long id) {
        carRepository.deleteById(id);
        return "Car deleted";
    }

    @GetMapping("/available")
    public List<Car> available(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return carRepository.findAvailable(start, end);
    }

}