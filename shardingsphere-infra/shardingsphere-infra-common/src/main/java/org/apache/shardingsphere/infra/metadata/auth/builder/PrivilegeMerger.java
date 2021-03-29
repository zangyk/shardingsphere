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

package org.apache.shardingsphere.infra.metadata.auth.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Privilege merger.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrivilegeMerger {
    
    /**
     * Merge privilege.
     * 
     * @param authentication authentication
     * @param schemaName schema name
     * @param rules ShardingSphere rules
     * @return privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivilege> merge(final Map<ShardingSphereUser, Collection<ShardingSpherePrivilege>> authentication,
                                                                         final String schemaName, final Collection<ShardingSphereRule> rules) {
        Map<ShardingSphereUser, ShardingSpherePrivilege> result = new HashMap<>(authentication.size(), 1);
        for (Entry<ShardingSphereUser, Collection<ShardingSpherePrivilege>> entry : authentication.entrySet()) {
            result.put(entry.getKey(), merge(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private static ShardingSpherePrivilege merge(final ShardingSphereUser user, final Collection<ShardingSpherePrivilege> privileges) {
        if (privileges.isEmpty()) {
            return new ShardingSpherePrivilege();
        }
        Iterator<ShardingSpherePrivilege> iterator = privileges.iterator();
        ShardingSpherePrivilege result = iterator.next();
        while (iterator.hasNext()) {
            ShardingSpherePrivilege each = iterator.next();
            if (!result.equals(each)) {
                throw new ShardingSphereException("Different physical instances have different permissions for user %s@%s", user.getGrantee().getUsername(), user.getGrantee().getHostname());
            }
        }
        return result;
    }
}
