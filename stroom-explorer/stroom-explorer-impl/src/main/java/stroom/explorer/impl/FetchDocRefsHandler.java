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
 */

package stroom.explorer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.docref.DocRef;
import stroom.explorer.api.ExplorerNodeService;
import stroom.explorer.shared.ExplorerNode;
import stroom.explorer.shared.FetchDocRefsAction;
import stroom.explorer.shared.SharedDocRef;
import stroom.security.api.SecurityContext;
import stroom.security.shared.DocumentPermissionNames;
import stroom.task.api.AbstractTaskHandler;
import stroom.util.shared.SharedSet;

import javax.inject.Inject;


class FetchDocRefsHandler
        extends AbstractTaskHandler<FetchDocRefsAction, SharedSet<SharedDocRef>> {
    private final Logger LOGGER = LoggerFactory.getLogger(FetchDocRefsHandler.class);
    private final ExplorerNodeService explorerNodeService;
    private final SecurityContext securityContext;

    @Inject
    FetchDocRefsHandler(final ExplorerNodeService explorerNodeService,
                        final SecurityContext securityContext) {
        this.explorerNodeService = explorerNodeService;
        this.securityContext = securityContext;
    }

    @Override
    public SharedSet<SharedDocRef> exec(final FetchDocRefsAction action) {
        return securityContext.secureResult(() -> {
            final SharedSet<SharedDocRef> result = new SharedSet<>();
            if (action.getDocRefs() != null) {
                for (final DocRef docRef : action.getDocRefs()) {
                    try {
                        // Only return entries the user has permission to see.
                        if (securityContext.hasDocumentPermission(docRef.getType(), docRef.getUuid(), DocumentPermissionNames.USE)) {
                            explorerNodeService.getNode(docRef)
                                    .map(ExplorerNode::getDocRef)
                                    .map(SharedDocRef::create)
                                    .ifPresent(result::add);
                        }
                    } catch (final RuntimeException e) {
                        LOGGER.debug(e.getMessage(), e);
                    }
                }
            }

            return result;
        });
    }
}
