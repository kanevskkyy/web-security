package com.example.javawebapplication.controller;

import com.example.javawebapplication.dto.request.OrderRequest;
import com.example.javawebapplication.dto.response.OrderResponse;
import com.example.javawebapplication.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders(@RequestParam(required = false) Long productId) {
        if (productId != null) {
            return ResponseEntity.ok(orderService.getOrdersByProduct(productId));
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
