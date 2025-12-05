package com.customer.controller;

import com.customer.dto.request.CreateCustomerRequest;
import com.customer.dto.response.CustomerResponse;
import com.customer.dto.request.UpdateCustomerRequest;
import com.customer.entity.Customer;
import com.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    // ----------------------------
    // Create Customer
    // ----------------------------
    @Test
    void testCreateCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("Sylvia");
        customer.setEmail("sylvia@gmail.com");
        customer.setPhone("0812345678");
        customer.setAddress("Sylvia Address");
        customer.setCreatedAt(LocalDateTime.now());

        Mockito.when(customerService.createCustomer(any(CreateCustomerRequest.class)))
                .thenReturn(customer);

        mockMvc.perform(post("/api/customers/createNewCustomer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "Sylvia",
                        "email": "sylvia@mail.com",
                        "address": "Sylvia Address",
                        "phone": "0812345678"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sylvia"));
    }

    // ----------------------------
    // Get Customer by ID
    // ----------------------------
    @Test
    void testGetCustomerById() throws Exception {
        UUID id = UUID.randomUUID();

        CustomerResponse response = new CustomerResponse(id, "Sylvia", "sylvia@mail.com", "0812345678", "Sylvia Address");

        Mockito.when(customerService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/customers/getCustomerById/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sylvia"));
    }

    // ----------------------------
    // Get All Customers
    // ----------------------------
    @Test
    void testGetAllCustomers() throws Exception {
        Customer customer = new Customer();
        customer.setName("Sylvia");

        Page<Customer> page = new PageImpl<>(List.of(customer));

        Mockito.when(customerService.getAllCustomers(0, 10))
                .thenReturn(page);

        mockMvc.perform(get("/api/customers/getAllCustomers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sylvia"));
    }

    // ----------------------------
    // Search by Name
    // ----------------------------
    @Test
    void testSearchByName() throws Exception {
        Customer customer = new Customer();
        customer.setName("Sylvia");

        Page<Customer> page = new PageImpl<>(List.of(customer));

        Mockito.when(customerService.getAllCustomersByName(eq("Syl"), eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/customers/searchByCustomerName")
                        .param("name", "Syl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sylvia"));
    }

    // ----------------------------
    // Update
    // ----------------------------
    @Test
    void testUpdateCustomer() throws Exception {
        UUID id = UUID.randomUUID();

        Customer updated = new Customer();
        updated.setId(id);
        updated.setName("Sylvia Updated");

        Mockito.when(customerService.updateCustomer(eq(id), any(UpdateCustomerRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/customers/updateCustomerById/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name": "Sylvia Updated"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sylvia Updated"));
    }

    // ----------------------------
    // Delete (Success)
    // ----------------------------
    @Test
    void testDeleteCustomerSuccess() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(customerService.exists(id)).thenReturn(true);

        mockMvc.perform(delete("/api/customers/deleteCustomerById/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer deleted successfully"));
    }

    // ----------------------------
    // Delete (Not Found)
    // ----------------------------
    @Test
    void testDeleteCustomerNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(customerService.exists(id)).thenReturn(false);

        mockMvc.perform(delete("/api/customers/deleteCustomerById/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Customer not found"));
    }

}

