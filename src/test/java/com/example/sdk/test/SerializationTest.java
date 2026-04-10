package com.example.sdk.test;

import com.example.sdk.invoker.JSON;
import com.example.sdk.model.Customer;
import java.util.UUID;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SerializationTest {



    @Test
    public void testCustomerDeserialization() {
        String json = """
            {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "name": "Acme Corp",
                "status": "ACTIVE",
                "address": {
                    "street": "123 Main St",
                    "city": "Tech City",
                    "postalCode": "12345"
                }
            }
        """;

        JSON serializer = new GsonJsonSerializer();
        Customer customer = serializer.deserialize(json, Customer.class);

        assertNotNull(customer);
        assertEquals("Acme Corp", customer.name());
        assertEquals(Customer.StatusEnum.ACTIVE, customer.status());
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), customer.id());
        assertNotNull(customer.address());
        assertEquals("123 Main St", customer.address().street());
    }
}
