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

package org.apache.shardingsphere.infra.auth.builtin.yaml.swapper;

import org.apache.shardingsphere.infra.metadata.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.config.YamlUserRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.swapper.UserRuleYamlSwapper;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.Grantee;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class UserRuleYamlSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        DefaultAuthentication authentication = new DefaultAuthentication();
        authentication.getAuthentication().put(new ShardingSphereUser("user1", "pwd1", "127.0.0.1"), new ShardingSpherePrivilege());
        authentication.getAuthentication().put(new ShardingSphereUser("user2", "pwd2", "127.0.0.2"), new ShardingSpherePrivilege());
        YamlUserRuleConfiguration actual = new UserRuleYamlSwapper().swapToYamlConfiguration(authentication.getAllUsers());
        assertThat(actual.getUsers().size(), is(2));
        assertThat(actual.getUsers().get("user1").getPassword(), is("pwd1"));
        assertThat(actual.getUsers().get("user1").getHostname(), is("127.0.0.1"));
        assertThat(actual.getUsers().get("user2").getPassword(), is("pwd2"));
        assertThat(actual.getUsers().get("user2").getHostname(), is("127.0.0.2"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlUserConfiguration user1 = new YamlUserConfiguration();
        user1.setPassword("pwd1");
        YamlUserConfiguration user2 = new YamlUserConfiguration();
        user2.setPassword("pwd2");
        Map<String, YamlUserConfiguration> users = new HashMap<>(2, 1);
        users.put("user1", user1);
        users.put("user2", user2);
        YamlUserRuleConfiguration yamlConfig = new YamlUserRuleConfiguration();
        yamlConfig.setUsers(users);
        Collection<ShardingSphereUser> actual = new UserRuleYamlSwapper().swapToObject(yamlConfig);
        Optional<ShardingSphereUser> actualUser1 = actual.stream().filter(each -> each.getGrantee().equals(new Grantee("user1", ""))).findFirst();
        assertTrue(actualUser1.isPresent());
        Optional<ShardingSphereUser> actualUser2 = actual.stream().filter(each -> each.getGrantee().equals(new Grantee("user2", ""))).findFirst();
        assertTrue(actualUser2.isPresent());
    }
    
    @Test
    public void assertSwapToObjectForNull() {
        Collection<ShardingSphereUser> actual = new UserRuleYamlSwapper().swapToObject(null);
        assertTrue(actual.isEmpty());
    }
}
