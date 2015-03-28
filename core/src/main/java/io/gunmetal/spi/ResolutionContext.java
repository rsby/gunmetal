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

package io.gunmetal.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rees.byars
 */
public interface ResolutionContext {

    ProvisionContext provisionContext(ResourceMetadata<?> resourceMetadata);

    void setParam(Dependency dependency, Object value);

    Object getParam(Dependency dependency);

    static ResolutionContext create() {
        return new Internal.ResolutionContextImpl();
    }

    interface States {
        byte NEW = 0;
        byte PRE_INSTANTIATION = 1;
        byte PRE_INJECTION = 2;
    }

    final class ProvisionContext {
        public byte state = States.NEW;
        public Object provision;
        public boolean attemptedCircularResolution = false;
    }

    final class Internal {

        private static class ResolutionContextImpl implements ResolutionContext {

            private final Map<ResourceMetadata<?>, ProvisionContext> contextMap = new HashMap<>();
            private Map<Dependency, Object> params;

            @Override public ProvisionContext provisionContext(ResourceMetadata<?> resourceMetadata) {

                ProvisionContext strategyContext = contextMap.get(resourceMetadata);

                if (strategyContext == null) {
                    strategyContext = new ProvisionContext();
                    contextMap.put(resourceMetadata, strategyContext);
                }

                return strategyContext;
            }

            @Override public void setParam(Dependency dependency, Object value) {
                if (params == null) {
                    params = new HashMap<>();
                }
                params.put(dependency, value);
            }

            @Override public Object getParam(Dependency dependency) {
                if (params == null) {
                    return null;
                }
                return params.get(dependency);
            }
        }

    }
}
