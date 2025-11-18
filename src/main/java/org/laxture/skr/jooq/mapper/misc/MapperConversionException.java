/*
 * Copyright (C) 2019-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.laxture.skr.jooq.mapper.misc;

import java.lang.reflect.Type;

/**
 * Exception thrown during mapperconversion operations.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class MapperConversionException extends RuntimeException {

    /**
     * Constructs a new conversion exception with source and target types.
     *
     * @param sourceType the source type
     * @param targetType the target type
     */
    public MapperConversionException(Type sourceType, Type targetType) {
        super("Cannot convert " + sourceType.getTypeName() + " to " + targetType.getTypeName());
    }

    /**
     * Constructs a new conversion exception with source and target types and a cause.
     *
     * @param sourceType the source type
     * @param targetType the target type
     * @param cause the cause of the conversion failure
     */
    public MapperConversionException(Type sourceType, Type targetType, Throwable cause) {
        super("Cannot convert " + sourceType.getTypeName() + " to " + targetType.getTypeName()
                + ": " + cause.getMessage(), cause);
    }

}
