package com.umar.carrental;

import com.jayway.jsonpath.JsonPath;
import com.umar.carrental.model.Role;
import com.umar.carrental.model.User;
import com.umar.carrental.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=YEAR",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
        "jwt.expiration=86400000",
        "app.cors.origin=http://localhost:5173"
})
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final String AUTH = "Authorization";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private static final String CAR_JSON =
            "{\"brand\":\"Toyota\",\"model\":\"Corolla\",\"year\":2023,\"pricePerDay\":9000,\"available\":true}";

    private String newEmail() {
        return "user" + UUID.randomUUID() + "@test.com";
    }

    private void register(String email) throws Exception {
        String body = "{\"name\":\"Test\",\"email\":\"" + email + "\",\"password\":\"123456\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private String login(String email) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"123456\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private String userToken() throws Exception {
        String email = newEmail();
        register(email);
        return login(email);
    }

    private String adminToken() throws Exception {
        String email = newEmail();
        register(email);
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        return login(email);
    }

    private long addCar(String adminToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/cars")
                        .header(AUTH, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CAR_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private String bookingJson(long carId, String start, String end) {
        return String.format("{\"carId\":%d,\"startDate\":\"%s\",\"endDate\":\"%s\"}", carId, start, end);
    }

    @Test
    void carsListIsPublic() throws Exception {
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk());
    }

    @Test
    void registerThenLoginReturnsToken() throws Exception {
        String email = newEmail();
        register(email);
        String body = "{\"email\":\"" + email + "\",\"password\":\"123456\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void wrongPasswordGives401() throws Exception {
        String email = newEmail();
        register(email);
        String body = "{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addCarWithoutTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CAR_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void normalUserCannotAddCar() throws Exception {
        String token = userToken();
        mockMvc.perform(post("/api/cars")
                        .header(AUTH, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CAR_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAddCar() throws Exception {
        String token = adminToken();
        mockMvc.perform(post("/api/cars")
                        .header(AUTH, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CAR_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Toyota"));
    }

    @Test
    void fullBookingFlow() throws Exception {
        String adminToken = adminToken();
        long carId = addCar(adminToken);
        String userToken = userToken();

        String start = LocalDate.now().plusDays(10).toString();
        String end = LocalDate.now().plusDays(12).toString();

        MvcResult first = mockMvc.perform(post("/api/bookings")
                        .header(AUTH, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(carId, start, end)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(27000.0))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn();
        Number bookingId = JsonPath.read(first.getResponse().getContentAsString(), "$.id");

        // same dates again must be rejected
        mockMvc.perform(post("/api/bookings")
                        .header(AUTH, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(carId, start, end)))
                .andExpect(status().isConflict());

        // booking without token must be rejected
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(carId, start, end)))
                .andExpect(status().isForbidden());

        // another user cannot cancel this booking
        String otherToken = userToken();
        mockMvc.perform(patch("/api/bookings/{id}/cancel", bookingId.longValue())
                        .header(AUTH, "Bearer " + otherToken))
                .andExpect(status().isForbidden());

        // owner cancels
        mockMvc.perform(patch("/api/bookings/{id}/cancel", bookingId.longValue())
                        .header(AUTH, "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // after cancel the same dates are free again
        mockMvc.perform(post("/api/bookings")
                        .header(AUTH, "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(carId, start, end)))
                .andExpect(status().isOk());
    }
}