/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal;

import groovy.lang.Closure;
import org.gradle.api.Namer;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.util.Instantiator;

public class FactoryNamedDomainObjectContainer<T> extends AbstractNamedDomainObjectContainer<T> {
    private final NamedDomainObjectFactory<T> factory;

    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator, Namer<? super T> namer, NamedDomainObjectFactory<T> factory) {
        super(type, classGenerator, namer);
        this.factory = factory;
    }

    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator, Namer<? super T> namer) {
        this(type, classGenerator, namer, new ReflectiveObjectFactory<T>(type));
    }

    /**
     * @param creationArgs Extra args passed to the constructor of auto created objects, after the first name argument.
     */
    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator, Namer<? super T> namer, Object... creationArgs) {
        this(type, classGenerator, namer, new ReflectiveObjectFactory<T>(type, creationArgs));
    }

    public FactoryNamedDomainObjectContainer(Class<T> type, ClassGenerator classGenerator, Namer<? super T> namer, final Closure factoryClosure) {
        this(type, classGenerator, namer, new ClosureObjectFactory<T>(type, factoryClosure));
    }

    @Override
    protected T doCreate(String name) {
        return factory.create(name);
    }

    protected static class ReflectiveObjectFactory<T> implements NamedDomainObjectFactory<T> {
        private final Class<? extends T> type;
        private final Object[] extraArgs;

        public ReflectiveObjectFactory(Class<? extends T> type, Object... extraArgs) {
            this.type = type;
            this.extraArgs = extraArgs;
        }

        public T create(String name) {
            Instantiator instantiator = new Instantiator();
            return instantiator.newInstance(type, combineInstantiationArgs(name));
        }

        protected Object[] combineInstantiationArgs(String name) {
            Object[] combinedArgs;
            if (extraArgs.length == 0) {
                Object[] nameArg = {name};
                combinedArgs = nameArg;
            } else {
                combinedArgs = new Object[extraArgs.length + 1];
                combinedArgs[0] = name;
                int i = 1;
                for (Object e : extraArgs) {
                    combinedArgs[i++] = e;
                }
            }

            return combinedArgs;
        }
    }

    private static class ClosureObjectFactory<T> implements NamedDomainObjectFactory<T> {
        private final Class<T> type;
        private final Closure factoryClosure;

        public ClosureObjectFactory(Class<T> type, Closure factoryClosure) {
            this.type = type;
            this.factoryClosure = factoryClosure;
        }

        public T create(String name) {
            return type.cast(factoryClosure.call(name));
        }
    }
}
