package com.shopsphere.web;

import com.shopsphere.config.AppConfig;
import com.shopsphere.db.Database;
import com.shopsphere.db.MigrationRunner;
import com.shopsphere.service.AuthService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application lifecycle listener.
 *
 * <p>On start-up the versioned schema migrations are applied and, when no
 * administrator exists yet, the bootstrap administrator is created. On shutdown the
 * connection pool is closed.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        AppConfig config = AppConfig.get();
        LOG.info("ShopSphere starting. Configuration source: " + config.configSource());

        try {
            MigrationRunner.Result result = MigrationRunner.migrate();
            LOG.info("Database schema is up to date. Applied now: " + result.applied()
                    + ", already present: " + result.alreadyApplied());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not apply the database migrations. "
                    + "The application will start, but database features will fail.", e);
        }

        try {
            new AuthService().bootstrapAdmin();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Could not create the bootstrap administrator", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            Database.close();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Error while closing the connection pool", e);
        }
    }
}
