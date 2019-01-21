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

package stroom.streamtask.testshared;


import org.junit.jupiter.api.Test;
import stroom.processor.shared.ProcessorFilterTracker;
import stroom.util.test.StroomUnitTest;

import static org.assertj.core.api.Assertions.assertThat;

class TestStreamProcessorFilterTracker extends StroomUnitTest {
    @Test
    void testPercent() {
        final ProcessorFilterTracker track = new ProcessorFilterTracker();
        track.setMinStreamCreateMs(0L);
        track.setMaxStreamCreateMs(100L);
        track.setStreamCreateMs(50L);

        assertThat(track.getTrackerStreamCreatePercentage().intValue()).isEqualTo(50);
    }

    @Test
    void testPercentReal() {
        final ProcessorFilterTracker track = new ProcessorFilterTracker();
        track.setMinStreamCreateMs(1413456996578L);
        track.setMaxStreamCreateMs(1413553741788L);
        track.setStreamCreateMs(1413553752020L);

        assertThat(track.getTrackerStreamCreatePercentage().intValue()).isEqualTo(100);
    }
}
