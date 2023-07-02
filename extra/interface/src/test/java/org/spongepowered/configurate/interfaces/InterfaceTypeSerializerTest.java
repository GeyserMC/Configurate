/*
 * Configurate
 * Copyright (C) zml and Configurate contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spongepowered.configurate.interfaces;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;

class InterfaceTypeSerializerTest {

    @Test
    void testDeserialization() throws ConfigurateException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(InterfaceDefaultOptions.get());
        // doesn't deserialize if value is NullValue
        node.node("hello").set("world");

        final @Nullable TestConfig config = Assertions.assertDoesNotThrow(() -> node.get(TestConfig.class));
        assertNotNull(config);
        assertInstanceOf(TestConfigImpl.class, config);
    }

    @Test
    void testInnerDeserialization() throws ConfigurateException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(InterfaceDefaultOptions.get());
        // doesn't deserialize if value is NullValue
        node.node("hello").set("world");

        final TestConfig.@Nullable TestInnerConfig config =
            Assertions.assertDoesNotThrow(() -> node.get(TestConfig.TestInnerConfig.class));
        assertNotNull(config);
        assertInstanceOf(TestConfigImpl.TestInnerConfig.class, config);
    }

}
