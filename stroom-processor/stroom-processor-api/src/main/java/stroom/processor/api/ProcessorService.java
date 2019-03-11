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

package stroom.processor.api;

import stroom.docref.DocRef;
import stroom.processor.shared.FindProcessorCriteria;
import stroom.processor.shared.Processor;
import stroom.processor.shared.ProcessorFilter;
import stroom.processor.shared.QueryData;
import stroom.util.logging.LambdaLogger;
import stroom.util.shared.BaseResultList;
import stroom.util.shared.HasIntCrud;

import java.util.List;
import java.util.Optional;

public interface ProcessorService extends HasIntCrud<Processor> {
    Processor create(DocRef pipelineRef, boolean enabled);

//    Optional<Processor> fetchByUuid(final String uuid);
//
//    default Processor fetchByUuidOrThrow(final String uuid) {
//        return fetchByUuid(uuid)
//                .orElseThrow(() -> new RuntimeException(
//                        LambdaLogger.buildMessage("Could not find processor with UUID {}", uuid)));
//    }

    BaseResultList<Processor> find(FindProcessorCriteria criteria);
}
