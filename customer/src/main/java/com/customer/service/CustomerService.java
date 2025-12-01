package com.customer.service;

import com.customer.dto.CreateCustomerRequest;
import com.customer.dto.LoginRequest;
import com.customer.dto.UpdateCustomerRequest;
import com.customer.entity.Customer;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface CustomerService {

    Customer createCustomer(CreateCustomerRequest request);

    Customer updateCustomer(UUID id, UpdateCustomerRequest request);

    boolean login(LoginRequest request);

    Page<Customer> getAllCustomers(int page, int size);

    void deleteCustomer(UUID id);

    Customer getById(UUID id);

    Page<Customer> getAllCustomersByName(String name, int page, int size);
}
