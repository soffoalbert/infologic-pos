package com.infologic.pos.config.tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MultiTenantSchemaConnectionProvider implements MultiTenantConnectionProvider, ApplicationContextAware {

    private static final long serialVersionUID = 1L;

    @Autowired
    private transient DataSource dataSource;
    
    private final Map<String, Boolean> tenantSchemaCache = new HashMap<>();

    /**
     * No-argument constructor required by Hibernate
     */
    public MultiTenantSchemaConnectionProvider() {
        log.debug("MultiTenantSchemaConnectionProvider initialized with no-arg constructor");
    }
    
    /**
     * Constructor with explicit DataSource, kept for backward compatibility
     */
    public MultiTenantSchemaConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
        log.debug("MultiTenantSchemaConnectionProvider initialized with dataSource");
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // If dataSource is null, try to get it from the application context
        if (this.dataSource == null) {
            try {
                this.dataSource = applicationContext.getBean(DataSource.class);
                log.debug("DataSource obtained from ApplicationContext");
            } catch (BeansException e) {
                log.error("Failed to obtain DataSource from ApplicationContext", e);
            }
        }
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized - it may be that the provider was instantiated by Hibernate before Spring could perform dependency injection");
        }
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        log.debug("Getting connection for tenant: {}", tenantIdentifier);
        final Connection connection = getAnyConnection();
        
        try {
            // Set schema for this connection
            String tenantId = (String) tenantIdentifier;
            connection.createStatement().execute(String.format("SET search_path TO %s", tenantId));
            return connection;
        } catch (SQLException e) {
            log.error("Error setting schema to {}", tenantIdentifier, e);
            connection.close();
            throw e;
        }
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        try {
            // Reset schema to default before returning the connection
            connection.createStatement().execute("SET search_path TO public");
        } catch (SQLException e) {
            log.warn("Could not reset search_path to public schema", e);
        }
        
        // Close the connection
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("Cannot unwrap");
    }
} 