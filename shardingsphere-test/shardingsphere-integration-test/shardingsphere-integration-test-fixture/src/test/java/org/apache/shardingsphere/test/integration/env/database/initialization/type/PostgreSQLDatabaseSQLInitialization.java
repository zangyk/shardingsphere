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

package org.apache.shardingsphere.test.integration.env.database.initialization.type;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.integration.env.database.initialization.DatabaseSQLInitialization;
import org.apache.shardingsphere.test.integration.env.datasource.builder.ActualDataSourceBuilder;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database SQL initialization for PostgreSQL.
 */
public final class PostgreSQLDatabaseSQLInitialization implements DatabaseSQLInitialization {
    
    @Override
    public void executeInitSQLs(final String scenario, final DatabaseType databaseType) throws IOException, JAXBException, SQLException {
        File file = new File(EnvironmentPath.getInitSQLFile(databaseType, scenario));
        for (String each : DatabaseEnvironmentManager.getDatabaseNames(scenario)) {
            // TODO use multiple threads to improve performance
            DataSource preDataSource = ActualDataSourceBuilder.build(null, scenario, databaseType);
            try (Connection connection = preDataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(String.format("SELECT pg_terminate_backend (pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '%s';", each));
                statement.execute(String.format("DROP DATABASE IF EXISTS %s;", each));
                statement.execute(String.format("CREATE DATABASE %s;", each));
            }
            DataSource dataSource = ActualDataSourceBuilder.build(each, scenario, databaseType);
            DatabaseEnvironmentManager.executeSQLScript(dataSource, file);
        }
    }
    
    @Override
    public String getType() {
        return new PostgreSQLDatabaseType().getName();
    }
}
