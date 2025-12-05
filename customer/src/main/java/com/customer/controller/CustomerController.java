package com.customer.controller;

import com.customer.dto.request.CreateCustomerRequest;
import com.customer.dto.response.CustomerResponse;
import com.customer.dto.request.UpdateCustomerRequest;
import com.customer.entity.Customer;
import com.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
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
    public ResponseEntity<CustomerResponse> getById(@PathVariable UUID id) {
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
    public ResponseEntity<?> deleteCustomer(@PathVariable UUID id) {
        boolean exists = customerService.exists(id);
        if (!exists) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "error", "Customer not found"
            ));
        }

        customerService.deleteCustomer(id);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Customer deleted successfully"
        ));
    }
}
