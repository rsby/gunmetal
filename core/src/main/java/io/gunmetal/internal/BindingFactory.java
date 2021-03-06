/*
 * Copyright (c) 2013.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gunmetal.internal;

import io.gunmetal.spi.DependencyRequest;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author rees.byars
 */
interface BindingFactory {

    List<Binding> createBindingsForModule(
            Class<?> module,
            boolean componentParam,
            ComponentContext context);

    Binding createParamBinding(
            Parameter parameter,
            ComponentContext context);

    Binding createJitBindingForRequest(
            DependencyRequest dependencyRequest,
            ComponentContext context);

    List<Binding> createJitFactoryBindingsForRequest(
            DependencyRequest dependencyRequest,
            ComponentContext context);
}
