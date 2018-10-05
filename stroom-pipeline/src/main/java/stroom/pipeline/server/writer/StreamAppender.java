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

package stroom.pipeline.server.writer;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import stroom.feed.server.FeedService;
import stroom.feed.shared.Feed;
import stroom.io.StreamCloser;
import stroom.pipeline.server.errorhandler.ErrorReceiverProxy;
import stroom.pipeline.server.errorhandler.ProcessException;
import stroom.pipeline.server.factory.ConfigurableElement;
import stroom.pipeline.server.factory.PipelineProperty;
import stroom.pipeline.server.factory.PipelinePropertyDocRef;
import stroom.pipeline.server.task.ProcessStatisticsFactory;
import stroom.pipeline.server.task.ProcessStatisticsFactory.ProcessStatistics;
import stroom.pipeline.shared.ElementIcons;
import stroom.pipeline.shared.data.PipelineElementType;
import stroom.pipeline.shared.data.PipelineElementType.Category;
import stroom.pipeline.state.MetaData;
import stroom.pipeline.state.RecordCount;
import stroom.pipeline.state.StreamHolder;
import stroom.pipeline.state.StreamProcessorHolder;
import stroom.query.api.v2.DocRef;
import stroom.streamstore.server.StreamStore;
import stroom.streamstore.server.StreamTarget;
import stroom.streamstore.server.StreamTypeService;
import stroom.streamstore.server.fs.serializable.RASegmentOutputStream;
import stroom.streamstore.shared.Stream;
import stroom.streamstore.shared.StreamType;
import stroom.util.io.ByteCountOutputStream;
import stroom.util.shared.Severity;
import stroom.util.spring.StroomScope;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;

@Component
@Scope(StroomScope.PROTOTYPE)
@ConfigurableElement(type = "StreamAppender", category = Category.DESTINATION, roles = {
        PipelineElementType.ROLE_TARGET, PipelineElementType.ROLE_DESTINATION,
        PipelineElementType.VISABILITY_STEPPING}, icon = ElementIcons.STREAM)
public class StreamAppender extends AbstractAppender {
    private final ErrorReceiverProxy errorReceiverProxy;
    private final StreamStore streamStore;
    private final StreamHolder streamHolder;
    private final FeedService feedService;
    private final StreamTypeService streamTypeService;
    private final StreamProcessorHolder streamProcessorHolder;
    private final MetaData metaData;
    private final RecordCount recordCount;
    private final StreamCloser streamCloser;

    private DocRef feedRef;
    private String streamType;
    private boolean segmentOutput = true;
    private StreamTarget streamTarget;
    private WrappedSegmentOutputStream wrappedSegmentOutputStream;
    private ByteCountOutputStream byteCountOutputStream;

    private ProcessStatistics lastProcessStatistics;

    @Inject
    public StreamAppender(final ErrorReceiverProxy errorReceiverProxy,
                          final StreamStore streamStore,
                          final StreamHolder streamHolder,
                          final FeedService feedService,
                          final StreamTypeService streamTypeService,
                          final StreamProcessorHolder streamProcessorHolder,
                          final MetaData metaData,
                          final RecordCount recordCount,
                          final StreamCloser streamCloser) {
        super(errorReceiverProxy);
        this.errorReceiverProxy = errorReceiverProxy;
        this.streamStore = streamStore;
        this.streamHolder = streamHolder;
        this.feedService = feedService;
        this.streamTypeService = streamTypeService;
        this.streamProcessorHolder = streamProcessorHolder;
        this.metaData = metaData;
        this.recordCount = recordCount;
        this.streamCloser = streamCloser;
    }

    @Override
    protected OutputStream createOutputStream() {
        final Stream parentStream = streamHolder.getStream();

        Feed feed;
        if (feedRef != null) {
            feed = feedService.loadByUuid(feedRef.getUuid());
        } else {
            if (parentStream == null) {
                throw new ProcessException("Unable to determine feed as no parent stream set");
            }

            // Use current feed if none other has been specified.
            feed = feedService.load(parentStream.getFeed());
        }

        if (streamType == null) {
            errorReceiverProxy.log(Severity.FATAL_ERROR, null, getElementId(), "Stream type not specified", null);
            throw new ProcessException("Stream type not specified");
        }
        final StreamType st = streamTypeService.loadByName(streamType);
        if (st == null) {
            errorReceiverProxy.log(Severity.FATAL_ERROR, null, getElementId(), "Stream type not specified", null);
            throw new ProcessException("Stream type not specified");
        }

        final Stream stream = Stream.createProcessedStream(parentStream, feed, st,
                streamProcessorHolder.getStreamProcessor(), streamProcessorHolder.getStreamTask());

        streamTarget = streamStore.openStreamTarget(stream);
        OutputStream targetOutputStream;

        // Let the stream closer handle closing it
        streamCloser.add(streamTarget);

        if (segmentOutput) {
            wrappedSegmentOutputStream = new WrappedSegmentOutputStream(new RASegmentOutputStream(streamTarget)) {
                @Override
                public void close() throws IOException {
                    super.flush();
                    super.close();
                    StreamAppender.this.close();
                }
            };
            targetOutputStream = wrappedSegmentOutputStream;

        } else {
            byteCountOutputStream = new ByteCountOutputStream(streamTarget.getOutputStream()) {
                @Override
                public void close() throws IOException {
                    super.flush();
                    super.close();
                    StreamAppender.this.close();
                }
            };
            targetOutputStream = byteCountOutputStream;
        }

        return targetOutputStream;
    }

    private void close() {
        // Only do something if an output stream was used.
        if (streamTarget != null) {
            // Write process meta data.
            streamTarget.getAttributeMap().putAll(metaData.getMetaMap());

            // Get current process statistics
            final ProcessStatistics processStatistics = ProcessStatisticsFactory.create(recordCount, errorReceiverProxy);
            // Diff the current statistics with the last captured statistics.
            final ProcessStatistics currentStatistics = processStatistics.substract(lastProcessStatistics);
            // Set the last statistics.
            lastProcessStatistics = processStatistics;

            // Write statistics meta data.
            currentStatistics.write(streamTarget.getAttributeMap());

            // We leave the streamCloser to close the stream target as it may
            // want to delete it instead
        }
    }

    @Override
    long getCurrentOutputSize() {
        if (wrappedSegmentOutputStream != null) {
            return wrappedSegmentOutputStream.getPosition();
        }
        if (byteCountOutputStream != null) {
            return byteCountOutputStream.getCount();
        }
        return 0;
    }

    @PipelinePropertyDocRef(types = Feed.ENTITY_TYPE)
    @PipelineProperty(description = "The feed that output stream should be written to. If not specified the feed the input stream belongs to will be used.")
    public void setFeed(final DocRef feedRef) {
        this.feedRef = feedRef;
    }

    @PipelineProperty(description = "The stream type that the output stream should be written as. This must be specified.")
    public void setStreamType(final String streamType) {
        this.streamType = streamType;
    }

    @PipelineProperty(description = "Should the output stream be marked with indexed segments to allow fast access to individual records?", defaultValue = "true")
    public void setSegmentOutput(final boolean segmentOutput) {
        this.segmentOutput = segmentOutput;
    }

    @SuppressWarnings("unused")
    @PipelineProperty(description = "When the current output stream exceeds this size it will be closed and a new one created.")
    public void setRollSize(final String size) {
        super.setRollSize(size);
    }

    @PipelineProperty(description = "Choose if you want to split aggregated streams into separate output streams.", defaultValue = "false")
    public void setSplitAggregatedStreams(final boolean splitAggregatedStreams) {
        super.setSplitAggregatedStreams(splitAggregatedStreams);
    }
}