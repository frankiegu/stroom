package stroom.security.impl.db;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.testcontainers.containers.MySQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDaoImplTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImplTest.class);

    private static MySQLContainer dbContainer = new MySQLContainer();

    private static Injector injector;
    private static UserDao userDao;

    @BeforeAll
    public static void beforeAll() {
        LOGGER.info(() -> "Before All - Start Database");
        dbContainer.start();

        injector = Guice.createInjector(new SecurityDbModule(), new ContainerSecurityConfigModule(dbContainer));

        userDao = injector.getInstance(UserDao.class);
    }

    @Test
    public void createSingleUser() {
        // Given
        final String userName = UUID.randomUUID().toString();

        // When
        final UserJooq userCreated = userDao.createUser(userName);

        // Then
        final UserJooq userFound = userDao.getUserByName(userName);
        assertThat(userFound.getName()).isEqualTo(userName);
        assertThat(userFound.getUuid()).isEqualTo(userCreated.getUuid());
    }

    @AfterAll
    public static void afterAll() {
        LOGGER.info(() -> "After All - Stop Database");
        dbContainer.stop();
    }
}
