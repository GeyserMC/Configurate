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

import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.function.UnaryOperator;

/**
 * This class has the default {@link ConfigurationOptions}
 * with {@link InterfaceTypeSerializer} added to the serializers.
 *
 * @since 4.2.0
 */
public final class InterfaceDefaultOptions {

    private static final ConfigurationOptions DEFAULTS =
        ConfigurationOptions.defaults()
            .serializers(
                TypeSerializerCollection.builder()
                    .registerAnnotated(InterfaceTypeSerializer::applicable, InterfaceTypeSerializer.INSTANCE)
                    .registerAll(TypeSerializerCollection.defaults())
                    .build()
            );

    private InterfaceDefaultOptions() {
    }

    /**
     * The default ConfigurationOptions with {@link InterfaceTypeSerializer} added to the serializers.
     *
     * @return the default ConfigurationOptions with {@link InterfaceTypeSerializer} added to the serializers.
     * @since 4.2.0
     */
    public static ConfigurationOptions get() {
        return DEFAULTS;
    }

    /**
     * Sets the default configuration options to be used by the resultant loader
     * by providing a function which takes the current {@link #get() default options}
     * and applies any desired changes.
     *
     * @param options to transform the existing default options
     * @return the default options with the applied changes
     * @since 4.2.0
     */
    public static ConfigurationOptions with(final UnaryOperator<ConfigurationOptions> options) {
        return options.apply(DEFAULTS);
    }

}
