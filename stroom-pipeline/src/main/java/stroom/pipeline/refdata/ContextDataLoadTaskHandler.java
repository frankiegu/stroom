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

package stroom.pipeline.refdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.data.shared.StreamTypeNames;
import stroom.feed.api.FeedProperties;
import stroom.meta.shared.Meta;
import stroom.pipeline.PipelineStore;
import stroom.pipeline.errorhandler.ErrorReceiverIdDecorator;
import stroom.pipeline.errorhandler.ErrorReceiverProxy;
import stroom.pipeline.errorhandler.StoredErrorReceiver;
import stroom.pipeline.factory.Pipeline;
import stroom.pipeline.factory.PipelineDataCache;
import stroom.pipeline.factory.PipelineFactory;
import stroom.pipeline.refdata.store.RefStreamDefinition;
import stroom.pipeline.shared.PipelineDoc;
import stroom.pipeline.shared.data.PipelineData;
import stroom.pipeline.state.FeedHolder;
import stroom.pipeline.state.MetaDataHolder;
import stroom.pipeline.state.MetaHolder;
import stroom.pipeline.task.StreamMetaDataProvider;
import stroom.security.api.SecurityContext;
import stroom.task.api.AbstractTaskHandler;
import stroom.util.io.BasicStreamCloser;
import stroom.util.io.StreamCloser;
import stroom.util.shared.Severity;
import stroom.util.shared.VoidResult;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;


class ContextDataLoadTaskHandler extends AbstractTaskHandler<ContextDataLoadTask, VoidResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextDataLoadTaskHandler.class);

    private final PipelineFactory pipelineFactory;
    private final RefDataLoaderHolder refDataLoaderHolder;
    private final FeedHolder feedHolder;
    private final FeedProperties feedProperties;
    private final MetaDataHolder metaDataHolder;
    private final ErrorReceiverProxy errorReceiverProxy;
    private final PipelineStore pipelineStore;
    private final MetaHolder metaHolder;
    private final PipelineDataCache pipelineDataCache;
    private final SecurityContext securityContext;

    private ErrorReceiverIdDecorator errorReceiver;

    @Inject
    ContextDataLoadTaskHandler(final PipelineFactory pipelineFactory,
                               final RefDataLoaderHolder refDataLoaderHolder,
                               final FeedHolder feedHolder,
                               final FeedProperties feedProperties,
                               final MetaDataHolder metaDataHolder,
                               final ErrorReceiverProxy errorReceiverProxy,
                               final PipelineStore pipelineStore,
                               final MetaHolder metaHolder,
                               final PipelineDataCache pipelineDataCache,
                               final SecurityContext securityContext) {
        this.pipelineFactory = pipelineFactory;
        this.refDataLoaderHolder = refDataLoaderHolder;
        this.feedHolder = feedHolder;
        this.feedProperties = feedProperties;
        this.metaDataHolder = metaDataHolder;
        this.errorReceiverProxy = errorReceiverProxy;
        this.pipelineStore = pipelineStore;
        this.metaHolder = metaHolder;
        this.pipelineDataCache = pipelineDataCache;
        this.securityContext = securityContext;
    }

    @Override
    public VoidResult exec(final ContextDataLoadTask task) {
        securityContext.secure(() -> {
            // Elevate user permissions so that inherited pipelines that the user only has 'Use' permission on can be read.
            securityContext.useAsRead(() -> {
                final StoredErrorReceiver storedErrorReceiver = new StoredErrorReceiver();
                errorReceiver = new ErrorReceiverIdDecorator(getClass().getSimpleName(), storedErrorReceiver);
                errorReceiverProxy.setErrorReceiver(errorReceiver);

                final InputStream inputStream = task.getInputStream();
                final Meta meta = task.getMeta();
                final String feedName = task.getFeedName();

                if (inputStream != null) {
                    final StreamCloser streamCloser = new BasicStreamCloser();
                    streamCloser.add(inputStream);

                    try {
                        String contextIdentifier = null;

                        if (LOGGER.isDebugEnabled()) {
                            final StringBuilder sb = new StringBuilder();
                            sb.append("(feed = ");
                            sb.append(feedName);
                            if (meta != null) {
                                sb.append(", source id = ");
                                sb.append(meta.getId());
                            }
                            sb.append(")");
                            contextIdentifier = sb.toString();
                            LOGGER.debug("Loading context data " + contextIdentifier);
                        }

                        // Create the parser.
                        final PipelineDoc pipelineDoc = pipelineStore.readDocument(task.getContextPipeline());
                        final PipelineData pipelineData = pipelineDataCache.get(pipelineDoc);
                        final Pipeline pipeline = pipelineFactory.create(pipelineData);

                        feedHolder.setFeedName(feedName);

                        // Setup the meta data holder.
                        metaDataHolder.setMetaDataProvider(new StreamMetaDataProvider(metaHolder, pipelineStore));

                        // Get the appropriate encoding for the stream type.
                        final String encoding = feedProperties.getEncoding(feedName, StreamTypeNames.CONTEXT);
//                    mapStoreHolder.setMapStoreBuilder(mapStoreBuilder);

                        // TODO is it always 0 for context streams?
//                    RefStreamDefinition refStreamDefinition = new RefStreamDefinition(
//                            pipelineDoc.getUuid(),
//                            pipelineDoc.getVersion(),
//                            stream.getId());

                        RefStreamDefinition refStreamDefinition = task.getRefStreamDefinition();

                        task.getRefDataStore().doWithLoaderUnlessComplete(
                                refStreamDefinition,
                                meta.getEffectiveMs(),
                                refDataLoader -> {
                                    // set this loader in the holder so it is available to the pipeline filters
                                    refDataLoaderHolder.setRefDataLoader(refDataLoader);
                                    // Process the boundary.
                                    try {
                                        // Parse the stream. The ReferenceDataFilter will process the context data
                                        pipeline.process(inputStream, encoding);

                                    } catch (final RuntimeException e) {
                                        log(Severity.FATAL_ERROR, e.getMessage(), e);
                                    }
                                });

                        // clear the reference to the loader now we have finished with it
                        refDataLoaderHolder.setRefDataLoader(null);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Finished loading context data " + contextIdentifier);
                        }
                    } catch (final RuntimeException e) {
                        log(Severity.FATAL_ERROR, "Error loading context data: " + e.getMessage(), e);
                    } finally {
                        try {
                            // Close all open streams.
                            streamCloser.close();
                        } catch (final IOException e) {
                            log(Severity.FATAL_ERROR, "Error closing context data stream: " + e.getMessage(), e);
                        }
                    }
                }
//            return loadedRefStreamDefinitions;
            });
        });
        return VoidResult.INSTANCE;
    }

    private void log(final Severity severity, final String message, final Throwable e) {
        LOGGER.debug(message, e);
        errorReceiver.log(severity, null, getClass().getSimpleName(), message, e);
    }
}
