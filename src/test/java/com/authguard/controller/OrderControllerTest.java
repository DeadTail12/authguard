package com.authguard.controller;

import com.authguard.dto.OrderResponse;
import com.authguard.exception.GlobalExceptionHandler;
import com.authguard.exception.OrderNotFoundException;
import com.authguard.security.SecurityConfig;
import com.authguard.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private OrderService orderService;

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forbidsAccessToAnOrderOutsideRegionAndNotOwned() throws Exception {
        when(orderService.getOrder(any(), eq(3L)))
                .thenThrow(new AccessDeniedException("Order 3 is not in your region and not yours"));

        mockMvc.perform(get("/api/v1/orders/3").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsNotFoundForMissingOrder() throws Exception {
        when(orderService.getOrder(any(), eq(999L))).thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/api/v1/orders/999").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsTheOrderWhenAccessIsAllowed() throws Exception {
        when(orderService.getOrder(any(), eq(1L)))
                .thenReturn(new OrderResponse(1L, "EU-WEST", "someone-else", "demo order"));

        mockMvc.perform(get("/api/v1/orders/1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("EU-WEST"));
    }
}
