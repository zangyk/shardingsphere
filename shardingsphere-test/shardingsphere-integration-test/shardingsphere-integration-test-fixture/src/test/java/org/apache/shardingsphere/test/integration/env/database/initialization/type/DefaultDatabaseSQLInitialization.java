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
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.integration.env.datasource.builder.ActualDataSourceBuilder;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Database SQL initialization for Default.
 */
public abstract class DefaultDatabaseSQLInitialization {
    
    /**
     * Execute init SQLs.
     *
     * @param scenario scenario
     * @param databaseType database type
     *
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public void executeInitSQLs(final String scenario, final DatabaseType databaseType) throws IOException, SQLException {
        // TODO use multiple threads to improve performance
        DataSource dataSource = ActualDataSourceBuilder.build(null, scenario, databaseType);
        File file = new File(EnvironmentPath.getInitSQLFile(databaseType, scenario));
        DatabaseEnvironmentManager.executeSQLScript(dataSource, file);
    }
}
