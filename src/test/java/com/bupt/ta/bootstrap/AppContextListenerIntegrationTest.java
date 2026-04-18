package com.bupt.ta.bootstrap;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppContextListenerIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void contextInitializedShouldBindSingleDatabaseInstance() {
        System.setProperty("ta105.data.dir", tempDir.toString());
        try {
            Map<String, Object> attributes = new HashMap<>();
            ServletContext servletContext = (ServletContext) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{ServletContext.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "setAttribute" -> {
                            attributes.put((String) args[0], args[1]);
                            yield null;
                        }
                        case "getAttribute" -> attributes.get((String) args[0]);
                        default -> null;
                    }
            );

            AppContextListener listener = new AppContextListener();
            listener.contextInitialized(new ServletContextEvent(servletContext));

            TaDatabase database = DatabaseProvider.get(servletContext);
            assertNotNull(database);
            assertSame(database, DatabaseProvider.get(servletContext));
            assertTrue(database.users().findByEmail("test@example.com").isPresent());
            assertTrue(database.users().findByEmail("mo@example.com").isPresent());
            assertTrue(database.users().findByEmail("admin@example.com").isPresent());
        } finally {
            System.clearProperty("ta105.data.dir");
        }
    }
}
