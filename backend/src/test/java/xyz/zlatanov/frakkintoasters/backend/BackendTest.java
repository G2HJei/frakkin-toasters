package xyz.zlatanov.frakkintoasters.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.UUID;

/**
 * A test against the application and a Testcontainers Postgres. The context, and with it the database, is shared by the
 * tests, so each test makes its own users and games.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Import(TestcontainersConfiguration.class)
public abstract class BackendTest {

    @Autowired
    protected JdbcClient jdbc;

    protected UUID createUser(String name) {
        var id = UUID.randomUUID();
        jdbc.sql("INSERT INTO app_user (id, name) VALUES (:id, :name)")
                .param("id", id)
                .param("name", name)
                .update();
        return id;
    }
}
