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

package stroom.streamstore;

import org.junit.Assert;
import org.junit.Test;
import stroom.streamstore.shared.FeedEntity;
import stroom.test.AbstractCoreIntegrationTest;
import stroom.test.CommonTestScenarioCreator;
import stroom.util.test.FileSystemTestUtil;

import javax.inject.Inject;

public class TestFeedServiceCache extends AbstractCoreIntegrationTest {
    @Inject
    private FeedEntityService feedService;
    @Inject
    private CommonTestScenarioCreator commonTestScenarioCreator;

    @Test
    public void testSimple() {
        final String feedName = FileSystemTestUtil.getUniqueTestString();
        final FeedEntity feed = feedService.getOrCreate(feedName);

        final FeedEntity loaded = feedService.get(feedName);

        Assert.assertNotNull(loaded);
        Assert.assertEquals(feed, loaded);
        Assert.assertSame(loaded, feedService.get(feedName));
        Assert.assertSame(loaded, feedService.get(feedName));
        Assert.assertSame(loaded, feedService.get(feedName));

//        loaded = cachedFeedService.loadById(feed.getId());
//        Assert.assertNotNull(loaded);
//        Assert.assertEquals(feed, loaded);
//        Assert.assertTrue(loaded == cachedFeedService.loadById(feed.getId()));
//        Assert.assertTrue(loaded == cachedFeedService.loadById(feed.getId()));
//        Assert.assertTrue(loaded == cachedFeedService.loadById(feed.getId()));
//
//        loaded = cachedFeedService.loadById(feed.getId());
//        Assert.assertNotNull(loaded);
//        Assert.assertEquals(feed, loaded);
//        Assert.assertTrue(loaded == cachedFeedService.loadById(feed.getId(), null));
//        Assert.assertTrue(loaded == cachedFeedService.loadById(feed.getId(), null));
//        Assert.assertTrue(loaded == cachedFeedService.loadById(feed.getId(), null));
    }
}