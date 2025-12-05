package com.customer.integration;

import com.customer.dto.request.CreateCustomerRequest;
import com.customer.dto.response.CustomerResponse;
import com.customer.dto.request.UpdateCustomerRequest;
import com.customer.entity.Customer;
import com.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    // ---------------------------------------------------------------------
    // CREATE CUSTOMER
    // ---------------------------------------------------------------------
    @Test
    void testCreateCustomer() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setName("Sylvia");
        request.setEmail("test@mail.com");
        request.setPhone("08123");

        Customer created = Customer.builder()
                .id(UUID.randomUUID())
                .name("Sylvia")
                .email("test@mail.com")
                .phone("08123")
                .build();

        Mockito.when(customerService.createCustomer(any(CreateCustomerRequest.class)))
                .thenReturn(created);

        mockMvc.perform(post("/api/customers/createNewCustomer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sylvia"));
    }

    // ---------------------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------------------
    @Test
    void testGetCustomerById() throws Exception {
        UUID id = UUID.randomUUID();

        CustomerResponse response = new CustomerResponse(
                id,
                "Sylvia",
                "email@test.com",
                "08123",
                "address"
        );

        Mockito.when(customerService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/customers/getCustomerById/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sylvia"));
    }

    // ---------------------------------------------------------------------
    // GET ALL CUSTOMERS
    // ---------------------------------------------------------------------
    @Test
    void testGetAllCustomers() throws Exception {
        List<Customer> customers = List.of(
                Customer.builder().id(UUID.randomUUID()).name("A").build(),
                Customer.builder().id(UUID.randomUUID()).name("B").build()
        );

        Page<Customer> page = new PageImpl<>(customers);

        Mockito.when(customerService.getAllCustomers(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/customers/getAllCustomers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    // ---------------------------------------------------------------------
    // SEARCH BY NAME
    // ---------------------------------------------------------------------
    @Test
    void testSearchByName() throws Exception {
        List<Customer> customers = List.of(
                Customer.builder().id(UUID.randomUUID()).name("Sylvia").build()
        );

        Page<Customer> page = new PageImpl<>(customers);

        Mockito.when(customerService.getAllCustomersByName("Sylvia", 0, 10))
                .thenReturn(page);

        mockMvc.perform(get("/api/customers/searchByCustomerName")
                        .param("name", "Sylvia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    // ---------------------------------------------------------------------
    // UPDATE CUSTOMER
    // ---------------------------------------------------------------------
    @Test
    void testUpdateCustomer() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setName("Updated Name");

        Customer updated = Customer.builder()
                .id(id)
                .name("Updated Name")
                .build();

        Mockito.when(customerService.updateCustomer(eq(id), any(UpdateCustomerRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/customers/updateCustomerById/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    // ---------------------------------------------------------------------
    // DELETE CUSTOMER - 200
    // ---------------------------------------------------------------------
    @Test
    void testDeleteCustomerSuccess() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(customerService.exists(id)).thenReturn(true);

        mockMvc.perform(delete("/api/customers/deleteCustomerById/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer deleted successfully"));
    }

    // ---------------------------------------------------------------------
    // DELETE CUSTOMER - 404
    // ---------------------------------------------------------------------
    @Test
    void testDeleteCustomerNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(customerService.exists(id)).thenReturn(false);

        mockMvc.perform(delete("/api/customers/deleteCustomerById/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Customer not found"));
    }
}
