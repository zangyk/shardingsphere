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

package org.apache.shardingsphere.infra.metadata.auth.refresher;

import org.apache.shardingsphere.infra.metadata.auth.Authentication;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.engine.MetadataRefresher;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * Authentication refresher.
 */
public interface AuthenticationRefresher extends MetadataRefresher {
    
    /**
     * Refresh.
     *
     * @param authentication authentication
     * @param sqlStatement sql statement
     * @param metaData metaData
     */
    void refresh(Authentication authentication, SQLStatement sqlStatement, ShardingSphereMetaData metaData);
}
