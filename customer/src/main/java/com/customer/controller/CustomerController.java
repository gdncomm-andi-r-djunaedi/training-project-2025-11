package com.customer.controller;

import com.customer.dto.CreateCustomerRequest;
import com.customer.dto.LoginRequest;
import com.customer.dto.UpdateCustomerRequest;
import com.customer.entity.Customer;
import com.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // ----------------------------
    // Create new customer
    // ----------------------------
    @PostMapping("/createNewCustomer")
    public ResponseEntity<Customer> createCustomer(
            @RequestBody CreateCustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    // ----------------------------
    // Get customer by ID
    // ----------------------------
    @GetMapping("/getCustomerById/{id}")
    public ResponseEntity<Customer> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    // ----------------------------
    // Get all customers
    // ----------------------------
    @GetMapping("/getAllCustomers")
    public ResponseEntity<Page<Customer>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(customerService.getAllCustomers(page, size));
    }

    // ----------------------------
    // Get all customers by name
    // ----------------------------
    @GetMapping("/searchByCustomerName")
    public ResponseEntity<Page<Customer>> getCustomerByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(customerService.getAllCustomersByName(name, page, size));
    }

    // ----------------------------
    // Update customer
    // ----------------------------
    @PutMapping("/updateCustomerById/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable UUID id,
            @RequestBody UpdateCustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    // ----------------------------
    // Delete customer
    // ----------------------------
    @DeleteMapping("/deleteCustomerById/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------
    // Login
    // ----------------------------
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest request
    ) {
        boolean success = customerService.login(request);

        if (success) {
            return ResponseEntity.ok("Login success");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
