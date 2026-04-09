package com.example.sdk.test;

import com.example.sdk.api.DefaultApi;
import com.example.sdk.invoker.ApiClient;
import com.example.sdk.model.Customer;
import com.github.tomakehurst.wiremock.WireMockServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WireMockIntegrationTest {

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(0); // dynamically allocated port
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    public void testGetCustomersEndpoint() throws Exception {
        // Stub the /customers response
        String jsonResponse = """
            [
              {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "name": "Integration Corp",
                "status": "ACTIVE"
              },
              {
                "id": "987e6543-e21b-34d5-c654-426614174111",
                "name": "Legacy LLC",
                "status": "INACTIVE"
              }
            ]
        """;

        stubFor(get(urlEqualTo("/customers"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(jsonResponse)));

        ApiClient apiClient = new ApiClient(
                new SerializationTest.GsonJsonSerializer(),
                "http://localhost:" + wireMockServer.port()
        );

        DefaultApi api = new DefaultApi(apiClient);

        List<Customer> customers = api.getCustomers();

        assertNotNull(customers);
        assertEquals(2, customers.size());

        Customer c1 = customers.get(0);
        assertEquals("Integration Corp", c1.name());
        assertEquals(Customer.StatusEnum.ACTIVE, c1.status());
        assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), c1.id());

        Customer c2 = customers.get(1);
        assertEquals("Legacy LLC", c2.name());
        assertEquals(Customer.StatusEnum.INACTIVE, c2.status());
    }
}
