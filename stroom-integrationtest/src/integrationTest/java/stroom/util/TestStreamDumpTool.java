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

package stroom.util;

import org.junit.Test;
import stroom.streamstore.store.api.StreamStore;
import stroom.streamstore.store.api.StreamTarget;
import stroom.data.meta.api.StreamProperties;
import stroom.streamstore.shared.StreamTypeNames;
import stroom.test.AbstractCoreIntegrationTest;
import stroom.util.io.FileUtil;
import stroom.util.io.StreamUtil;
import stroom.util.test.FileSystemTestUtil;

import javax.inject.Inject;
import java.io.IOException;

public class TestStreamDumpTool extends AbstractCoreIntegrationTest {
    @Inject
    private StreamStore streamStore;

    @Test
    public void test() throws IOException {
        final String feedName = FileSystemTestUtil.getUniqueTestString();

        try {
            addData(feedName, "This is some test data to dump");

            final StreamDumpTool streamDumpTool = new StreamDumpTool();
            streamDumpTool.setFeed(feedName);
            streamDumpTool.setOutputDir(FileUtil.getCanonicalPath(FileUtil.getTempDir()));
            streamDumpTool.run();

        } catch (final RuntimeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void addData(final String feedName, final String data) throws IOException {
        final StreamProperties streamProperties = new StreamProperties.Builder()
                .feedName(feedName)
                .streamTypeName(StreamTypeNames.RAW_EVENTS)
                .build();
        final StreamTarget streamTarget = streamStore.openStreamTarget(streamProperties);
        streamTarget.getOutputStream().write(data.getBytes(StreamUtil.DEFAULT_CHARSET));
        streamStore.closeStreamTarget(streamTarget);
    }
}
