package com.example.sdk.test;

import com.example.sdk.invoker.JSON;
import com.example.sdk.model.Customer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SerializationTest {

    public static class GsonJsonSerializer implements JSON {
        private final Gson gson = new GsonBuilder().create();

        @Override
        public String serialize(Object obj) {
            return gson.toJson(obj);
        }

        @Override
        public <T> T deserialize(String json, Type type) {
            return gson.fromJson(json, type);
        }
    }

    @Test
    public void testCustomerDeserialization() {
        String json = """
            {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "name": "Acme Corp",
                "status": "ACTIVE"
            }
        """;

        JSON serializer = new GsonJsonSerializer();
        Customer customer = serializer.deserialize(json, Customer.class);

        assertNotNull(customer);
        assertEquals("Acme Corp", customer.name());
        assertEquals(Customer.StatusEnum.ACTIVE, customer.status());
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), customer.id());
    }
}
