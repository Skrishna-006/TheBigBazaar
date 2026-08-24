package com.siva.shopsphere.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.authentication.AuthenticationProvider;

import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(controllers = TestExceptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void validationErrorsReturnBadRequestWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"bad-email\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.fieldErrors.password").value("Password is required"))
            .andExpect(jsonPath("$.fieldErrors.email").value("Invalid email"));
    }

    @Test
    void notFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/test/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void badRequestReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/test/bad-request"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid-json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void unexpectedErrorsDoNotExposeStackTrace() throws Exception {
        mockMvc.perform(get("/api/v1/test/unexpected"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("Internal Server Error"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }
}
