/*
 * Copyright 2017 Crown Copyright
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
 *
 */

package stroom.pipeline;

import stroom.entity.shared.PermissionException;
import stroom.pipeline.factory.PipelineDataValidator;
import stroom.pipeline.factory.PipelineStackLoader;
import stroom.pipeline.shared.FetchPipelineDataAction;
import stroom.pipeline.shared.PipelineDataMerger;
import stroom.pipeline.shared.PipelineDoc;
import stroom.pipeline.shared.data.PipelineData;
import stroom.pipeline.shared.data.PipelineElementType;
import stroom.pipeline.shared.data.SourcePipeline;
import stroom.security.api.SecurityContext;
import stroom.task.api.AbstractTaskHandler;
import stroom.util.shared.SharedList;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;


class FetchPipelineDataHandler extends AbstractTaskHandler<FetchPipelineDataAction, SharedList<PipelineData>> {
    private final PipelineStore pipelineStore;
    private final PipelineStackLoader pipelineStackLoader;
    private final PipelineDataValidator pipelineDataValidator;
    private final SecurityContext securityContext;

    @Inject
    FetchPipelineDataHandler(final PipelineStore pipelineStore,
                             final PipelineStackLoader pipelineStackLoader,
                             final PipelineDataValidator pipelineDataValidator,
                             final SecurityContext securityContext) {
        this.pipelineStore = pipelineStore;
        this.pipelineStackLoader = pipelineStackLoader;
        this.pipelineDataValidator = pipelineDataValidator;
        this.securityContext = securityContext;
    }

    @Override
    public SharedList<PipelineData> exec(final FetchPipelineDataAction action) {
        return securityContext.secureResult(() -> {
            try {
                final PipelineDoc pipelineDoc = pipelineStore.readDocument(action.getPipeline());

                // A user should be allowed to read pipelines that they are inheriting from as long as they have 'use' permission on them.
                return securityContext.useAsReadResult(() -> {
                    final List<PipelineDoc> pipelines = pipelineStackLoader.loadPipelineStack(pipelineDoc);
                    final SharedList<PipelineData> result = new SharedList<>(pipelines.size());

                    final Map<String, PipelineElementType> elementMap = PipelineDataMerger.createElementMap();
                    for (final PipelineDoc pipe : pipelines) {
                        final PipelineData pipelineData = pipe.getPipelineData();

                        // Validate the pipeline data and add element and property type
                        // information.
                        final SourcePipeline source = new SourcePipeline(pipe);
                        pipelineDataValidator.validate(source, pipelineData, elementMap);
                        result.add(pipelineData);
                    }

                    return result;
                });
            } catch (final PermissionException e) {
                throw new PermissionException(e.getUser(), e.getMessage().replaceAll("permission to read", "permission to use"));
            }
        });
    }
}
