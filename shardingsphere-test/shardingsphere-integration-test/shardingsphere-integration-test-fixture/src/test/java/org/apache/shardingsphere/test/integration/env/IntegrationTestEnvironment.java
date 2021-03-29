/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.integration.env;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.integration.env.database.embedded.EmbeddedDatabaseDistributionProperties;
import org.apache.shardingsphere.test.integration.env.database.embedded.EmbeddedDatabaseManager;
import org.apache.shardingsphere.test.integration.env.datasource.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.env.props.DatabaseScenarioProperties;
import org.apache.shardingsphere.test.integration.env.props.EnvironmentProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Integration test running environment.
 */
@Getter
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final EnvironmentType envType;
    
    private final Collection<String> adapters;
    
    private final Collection<String> scenarios;
    
    private final boolean runAdditionalTestCases;
    
    private Map<DatabaseType, Map<String, DataSourceEnvironment>> dataSourceEnvironments;
    
    private final Map<String, DataSourceEnvironment> proxyEnvironments;
    
    private IntegrationTestEnvironment() {
        Properties engineEnvProps = EnvironmentProperties.loadProperties("env/engine-env.properties");
        envType = getEnvironmentType(engineEnvProps);
        adapters = Splitter.on(",").trimResults().splitToList(engineEnvProps.getProperty("it.adapters"));
        scenarios = getScenarios(engineEnvProps);
        runAdditionalTestCases = Boolean.parseBoolean(engineEnvProps.getProperty("it.run.additional.cases"));
        Map<String, DatabaseScenarioProperties> databaseProps = getDatabaseScenarioProperties();
        dataSourceEnvironments = createDataSourceEnvironments(getDatabaseTypes(engineEnvProps), databaseProps);
        if (EnvironmentType.EMBEDDED == envType) {
            EmbeddedDatabaseDistributionProperties embeddedDatabaseProps = new EmbeddedDatabaseDistributionProperties(EnvironmentProperties.loadProperties("env/embedded-databases.properties"));
            dataSourceEnvironments = mergeDataSourceEnvironments(embeddedDatabaseProps);
            createEmbeddedDatabases(embeddedDatabaseProps);
        }
        proxyEnvironments = createProxyEnvironments(databaseProps);
    }
    
    private EnvironmentType getEnvironmentType(final Properties engineEnvProps) {
        try {
            return EnvironmentType.valueOf(engineEnvProps.getProperty("it.env.type"));
        } catch (final IllegalArgumentException ignored) {
            return EnvironmentType.NATIVE;
        }
    }
    
    private Collection<String> getScenarios(final Properties engineEnvProps) {
        Collection<String> result = Splitter.on(",").trimResults().splitToList(engineEnvProps.getProperty("it.scenarios"));
        for (String each : result) {
            EnvironmentPath.assertScenarioDirectoryExisted(each);
        }
        return result;
    }
    
    private Map<String, DatabaseScenarioProperties> getDatabaseScenarioProperties() {
        Map<String, DatabaseScenarioProperties> result = new HashMap<>(scenarios.size(), 1);
        for (String each : scenarios) {
            result.put(each, new DatabaseScenarioProperties(each, EnvironmentProperties.loadProperties(String.format("env/%s/scenario-env.properties", each))));
        }
        return result;
    }
    
    private Collection<DatabaseType> getDatabaseTypes(final Properties engineEnvProps) {
        return Arrays.stream(engineEnvProps.getProperty("it.databases").split(",")).map(each -> DatabaseTypeRegistry.getActualDatabaseType(each.trim())).collect(Collectors.toList());
    }
    
    private Map<DatabaseType, Map<String, DataSourceEnvironment>> createDataSourceEnvironments(final Collection<DatabaseType> databaseTypes, 
                                                                                               final Map<String, DatabaseScenarioProperties> databaseProps) {
        Map<DatabaseType, Map<String, DataSourceEnvironment>> result = new LinkedHashMap<>(databaseTypes.size(), 1);
        for (DatabaseType each : databaseTypes) {
            Map<String, DataSourceEnvironment> dataSourceEnvs = new LinkedHashMap<>(scenarios.size(), 1);
            for (String scenario : scenarios) {
                dataSourceEnvs.put(scenario, createDataSourceEnvironment(each, databaseProps.get(scenario)));
                result.put(each, dataSourceEnvs);
            }
        }
        return result;
    }
    
    private DataSourceEnvironment createDataSourceEnvironment(final DatabaseType databaseType, final DatabaseScenarioProperties databaseProps) {
        if (databaseType instanceof H2DatabaseType) {
            return new DataSourceEnvironment(databaseType, "", 0, "sa", "");
        }
        return new DataSourceEnvironment(databaseType, databaseProps.getDatabaseHost(databaseType), 
                databaseProps.getDatabasePort(databaseType), databaseProps.getDatabaseUsername(databaseType), databaseProps.getDatabasePassword(databaseType));
    }
    
    private Map<DatabaseType, Map<String, DataSourceEnvironment>> mergeDataSourceEnvironments(final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps) {
        Set<DatabaseType> databaseTypes = dataSourceEnvironments.keySet();
        Map<DatabaseType, Map<String, DataSourceEnvironment>> result = new LinkedHashMap<>(databaseTypes.size(), 1);
        for (DatabaseType each : databaseTypes) {
            Map<String, DataSourceEnvironment> dataSourceEnvs = new LinkedHashMap<>(scenarios.size(), 1);
            for (String scenario : scenarios) {
                dataSourceEnvs.put(scenario, mergeDataSourceEnvironment(dataSourceEnvironments.get(each).get(scenario), embeddedDatabaseProps));
                result.put(each, dataSourceEnvs);
            }
        }
        return result;
    }
    
    private DataSourceEnvironment mergeDataSourceEnvironment(final DataSourceEnvironment dataSourceEnvironment, final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps) {
        DatabaseType databaseType = dataSourceEnvironment.getDatabaseType();
        if (databaseType instanceof H2DatabaseType) {
            return new DataSourceEnvironment(databaseType, "", 0, "sa", "");
        }
        return new DataSourceEnvironment(databaseType, dataSourceEnvironment.getHost(),
                embeddedDatabaseProps.getInstancePort(databaseType), dataSourceEnvironment.getUsername(), dataSourceEnvironment.getPassword());
    }
    
    private void createEmbeddedDatabases(final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps) {
        for (Entry<DatabaseType, Map<String, DataSourceEnvironment>> entry : dataSourceEnvironments.entrySet()) {
            createEmbeddedDatabases(entry.getKey(), entry.getValue(), embeddedDatabaseProps);
        }
    }
    
    private void createEmbeddedDatabases(final DatabaseType databaseType,
                                         final Map<String, DataSourceEnvironment> dataSourceEnvs, final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps) {
        for (Entry<String, DataSourceEnvironment> entry : dataSourceEnvs.entrySet()) {
            EmbeddedDatabaseManager.startUp(databaseType.getName(), entry.getKey(), embeddedDatabaseProps, entry.getValue().getPort());
        }
    }
    
    private Map<String, DataSourceEnvironment> createProxyEnvironments(final Map<String, DatabaseScenarioProperties> databaseProps) {
        Map<String, DataSourceEnvironment> result = new HashMap<>(scenarios.size(), 1);
        for (String each : scenarios) {
            result.put(each, createProxyEnvironment(databaseProps.get(each)));
        }
        return result;
    }
    
    private DataSourceEnvironment createProxyEnvironment(final DatabaseScenarioProperties databaseProps) {
        // TODO hard code for MySQL, should configurable
        return new DataSourceEnvironment(
                new MySQLDatabaseType(), databaseProps.getProxyHost(), databaseProps.getProxyPort(), databaseProps.getProxyUsername(), databaseProps.getProxyPassword());
    }
    
    private void waitForEnvironmentReady(final String scenario) {
        int retryCount = 0;
        while (!isProxyReady(scenario) && retryCount < 30) {
            try {
                Thread.sleep(1000L);
            } catch (final InterruptedException ignore) {
            }
            retryCount++;
        }
    }
    
    @SuppressWarnings("CallToDriverManagerGetConnection")
    private boolean isProxyReady(final String scenario) {
        DataSourceEnvironment dataSourceEnv = proxyEnvironments.get(scenario);
        try (Connection connection = DriverManager.getConnection(dataSourceEnv.getURL(scenario), dataSourceEnv.getUsername(), dataSourceEnv.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
        } catch (final SQLException ignore) {
            return false;
        }
        return true;
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestEnvironment getInstance() {
        return INSTANCE;
    }
}
