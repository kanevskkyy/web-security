package com.example.javawebapplication;

import com.example.javawebapplication.config.SecurityConfig;
import com.example.javawebapplication.controller.CategoryController;
import com.example.javawebapplication.controller.OrderController;
import com.example.javawebapplication.controller.ProductController;
import com.example.javawebapplication.dto.request.CategoryRequest;
import com.example.javawebapplication.dto.request.OrderRequest;
import com.example.javawebapplication.dto.request.ProductRequest;
import com.example.javawebapplication.dto.response.CategoryResponse;
import com.example.javawebapplication.dto.response.OrderResponse;
import com.example.javawebapplication.dto.response.ProductResponse;
import com.example.javawebapplication.service.CategoryService;
import com.example.javawebapplication.service.OrderService;
import com.example.javawebapplication.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CategoryController.class, ProductController.class, OrderController.class})
@Import(SecurityConfig.class)
public class AccessSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("1. USER: GET /api/categories -> 200 OK")
    void test1_userCanReadCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/categories")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. USER: GET /api/products -> 200 OK")
    void test2_userCanReadProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/products")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("3. USER: POST /api/categories -> 403 Forbidden")
    void test3_userCannotCreateCategory() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Electronics\",\"description\":\"Gadgets\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. USER: DELETE /api/products/1 -> 403 Forbidden")
    void test4_userCannotDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                        .with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("5. MANAGER: POST /api/categories -> 201 Created")
    void test5_managerCanCreateCategory() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Smartphones", "Mobile phones");
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Smartphones\",\"description\":\"Mobile phones\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("6. MANAGER: POST /api/products -> 201 Created")
    void test6_managerCanCreateProduct() throws Exception {
        ProductResponse response = new ProductResponse(1L, "iPhone 15", "Apple smartphone",
                BigDecimal.valueOf(999.99), 50, 1L, "Smartphones");
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .with(httpBasic("manager", "manager123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"iPhone 15\",\"description\":\"Apple smartphone\",\"price\":999.99,\"stockQuantity\":50,\"categoryId\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("7. USER: POST /api/orders -> 201 Created")
    void test7_userCanCreateOrder() throws Exception {
        OrderResponse response = new OrderResponse(1L, "Oleksandr", "alex@example.com",
                1, BigDecimal.valueOf(999.99), BigDecimal.valueOf(999.99), LocalDateTime.now(), 1L, "iPhone 15");
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Oleksandr\",\"customerEmail\":\"alex@example.com\",\"quantity\":1,\"productId\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("8. MANAGER: DELETE /api/categories/1 -> 403 Forbidden")
    void test8_managerCannotDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/1")
                        .with(httpBasic("manager", "manager123")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("9. ADMIN: DELETE /api/orders/1 -> 204 No Content")
    void test9_adminCanDeleteOrder() throws Exception {
        mockMvc.perform(delete("/api/orders/1")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("10. UNAUTHENTICATED: GET /api/orders -> 401 Unauthorized")
    void test10_unauthenticatedCannotAccessOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }
}
