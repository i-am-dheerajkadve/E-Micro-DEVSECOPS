package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.cloud.gateway.enabled=false")
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Basic context loading test
    }
}
