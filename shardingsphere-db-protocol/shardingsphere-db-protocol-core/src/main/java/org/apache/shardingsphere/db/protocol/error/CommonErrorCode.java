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

package org.apache.shardingsphere.db.protocol.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Common error code.
 */
@RequiredArgsConstructor
@Getter
public enum CommonErrorCode implements SQLErrorCode {
    
    CIRCUIT_BREAK_MODE(1000, "C1000", "Circuit break mode is ON."),
    
    SHARDING_TABLE_RULES_NOT_EXISTED(1101, "C1101", "Sharding table rule %s is not exist."),
    
    TABLES_IN_USED(1102, "C1102", "Can not drop rule, tables %s in the rule are still in used."),

    RESOURCE_IN_USED(1103, "C1103", "Can not drop resources, resources %s in the rule are still in used."),
    
    RESOURCE_NOT_EXIST(1104, "C1104", "Can not drop resources, resources %s do not exist."),
    
    REPLICA_QUERY_RULE_NOT_EXIST(1105, "C1105", "Replica query rule does not exist."),
    
    REPLICA_QUERY_RULE_DATA_SOURCE_NOT_EXIST(1106, "C1106", "Data sources %s in replica query rule do not exist."),
    
    ADD_REPLICA_QUERY_RULE_DATA_SOURCE_EXIST(1107, "C1107", "Can not add replica query rule, data sources %s in replica query rule already exists."),
    
    REPLICA_QUERY_RULE_EXIST(1108, "C1108", "Replica query rule already exists."),
    
    SHARDING_RULE_NOT_EXIST(1109, "C1109", "Sharding rule does not exist."),
    
    SHARDING_TABLE_RULE_EXIST(1110, "C1110", "Sharding table rules: [%s] already exists."),
    
    SCALING_JOB_NOT_EXIST(1201, "C1201", "Scaling job %s does not exist."),
    
    SCALING_OPERATE_FAILED(1209, "C1209", "Scaling Operate Failed: [%s]"),
    
    UNSUPPORTED_COMMAND(1998, "C1998", "Unsupported command: [%s]"),
    
    UNKNOWN_EXCEPTION(1999, "C1999", "Unknown exception: [%s]");
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
}
