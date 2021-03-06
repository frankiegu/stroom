/*
 * Copyright 2016 Crown Copyright
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

package stroom.cluster.impl;

import com.caucho.hessian.server.HessianServlet;
import stroom.cluster.api.ClusterCallService;
import stroom.cluster.api.ClusterCallServiceLocal;
import stroom.cluster.api.ServiceName;
import stroom.util.shared.IsServlet;

import javax.inject.Inject;
import java.util.Set;

class ClusterCallServiceRPC extends HessianServlet implements ClusterCallService, IsServlet {

    private static final Set<String> PATH_SPECS = Set.of("/clustercall.rpc");

    private final ClusterCallService clusterCallService;

    @Inject
    ClusterCallServiceRPC(final ClusterCallServiceLocal clusterCallService) {
        this.clusterCallService = clusterCallService;
    }

    @Override
    public Object call(final String sourceNode, final String targetNode, final ServiceName serviceName, final String methodName, final Class<?>[] parameterTypes, final Object[] args) {
        return clusterCallService.call(sourceNode, targetNode, serviceName, methodName, parameterTypes, args);
    }

    @Override
    public Set<String> getPathSpecs() {
        return PATH_SPECS;
    }
}
