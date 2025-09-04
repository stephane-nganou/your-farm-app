package de.farm.app.orders;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import de.farm.app.FarmingAppApplication;

/**
 *
 * @author StephaneWafo
 */
@SpringBootTest(classes= FarmingAppApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OrderServiceImplIT {

    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("test").withUsername("test").withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        pg.start();
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }

    @Autowired
    OrderService orderService;

    @Test
    void reservesInventoryAtomically() throws Exception {
        //
    }

}
