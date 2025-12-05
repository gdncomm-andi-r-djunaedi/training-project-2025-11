package com.customer.service;

import com.customer.dto.request.CreateCustomerRequest;
import com.customer.dto.response.CustomerResponse;
import com.customer.dto.request.UpdateCustomerRequest;
import com.customer.entity.Customer;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface CustomerService {

    Customer createCustomer(CreateCustomerRequest request);

    Customer updateCustomer(UUID id, UpdateCustomerRequest request);

    Page<Customer> getAllCustomers(int page, int size);

    void deleteCustomer(UUID id);

    CustomerResponse getById(UUID id);

    Page<Customer> getAllCustomersByName(String name, int page, int size);

    public boolean exists(UUID id);

    CustomerResponse validateLogin(String email, String password);
}
