/*
 * Copyright 2018 Crown Copyright
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

package stroom.cluster;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import stroom.entity.event.EntityEvent;
import stroom.task.TaskHandler;

public class ClusterModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ClusterCallService.class).annotatedWith(Names.named("clusterCallServiceLocal")).to(ClusterCallServiceLocal.class);
        bind(ClusterCallService.class).annotatedWith(Names.named("clusterCallServiceRemote")).to(ClusterCallServiceRemote.class);
        bind(ClusterNodeManager.class).to(ClusterNodeManagerImpl.class);

        final Multibinder<TaskHandler> taskHandlerBinder = Multibinder.newSetBinder(binder(), TaskHandler.class);
        taskHandlerBinder.addBinding().to(UpdateClusterStateTaskHandler.class);

        final Multibinder<EntityEvent.Handler> entityEventHandlerBinder = Multibinder.newSetBinder(binder(), EntityEvent.Handler.class);
        entityEventHandlerBinder.addBinding().to(ClusterNodeManagerImpl.class);
    }
}