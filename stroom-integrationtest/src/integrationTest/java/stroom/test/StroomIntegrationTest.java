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

package stroom.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.util.io.FileUtil;
import stroom.util.test.StroomTest;
import stroom.util.test.TestState;
import stroom.util.test.TestState.State;

import javax.inject.Inject;
import java.nio.file.Path;

/**
 * This class should be common to all component and integration tests.
 * <p>
 * It is safer if all test classes destroy the Spring context after running
 * tests in the class to avoid knock on effects in other tests.
 */
public abstract class StroomIntegrationTest implements StroomTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(StroomIntegrationTest.class);

    private static final boolean TEAR_DOWN_DATABASE_BETWEEEN_TESTS = true;
    private static boolean XML_SCHEMAS_DOWNLOADED = false;

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            LOGGER.info(String.format("Started test: %s::%s", description.getClassName(), description.getMethodName()));
        }
    };

    @Inject
    private CommonTestControl commonTestControl;

    @Inject
    private ContentImportService contentImportService;

    @BeforeClass
    public static void beforeClass() {
        final State state = TestState.getState();
        state.reset();
    }

    @AfterClass
    public static void afterClass() {
    }

    protected void onBefore() {
    }

    protected void onAfter() {
    }

    /**
     * Initialise required database entities.
     */
    @Before
    public void before() {
        final State state = TestState.getState();
        state.incrementTestCount();

        // Setup the database if this is the first test running for this test
        // class or if we always want to recreate the DB between tests.
        if (TEAR_DOWN_DATABASE_BETWEEEN_TESTS || getTestCount() == 1) {
            if (!state.isDoneSetup()) {
                LOGGER.info("before() - commonTestControl.setup()");
                commonTestControl.teardown();
                commonTestControl.setup();

                // Some test classes only want the DB to be created once so they
                // return true here.
                state.setDoneSetup(doSingleSetup());
            }
        }

        onBefore();
    }

    /**
     * Remove all entities from the database.
     */
    @After
    public void after() {
        onAfter();
    }

    /**
     * Some tests only want some setup to be performed before the first test is
     * executed and then no teardown to occur. If this is the case then they
     * should override this method, perform their one time setup task and then
     * return true.
     */
    protected boolean doSingleSetup() {
        return false;
    }

    /**
     * Remove all entities from the database and reinitialise required entities.
     */
    public final void clean() {
        clean(false);
    }

    /**
     * Remove all entities from the database and reinitialise required entities.
     */
    public final void clean(final boolean force) {
        // Only bother to clean the database if we have run at least one test in
        // this test class.
        if (force || getTestCount() > 1) {
            teardown(force);
            setup(force);
        }
    }

    /**
     * Initialise required database entities.
     */
    private void setup(final boolean force) {
        // Only bother to manually setup the database if we have run at least
        // one test in this test class.
        if (force || getTestCount() > 1) {
            commonTestControl.setup();
        }
    }

    void importSchemas(final boolean force) {
        if (force || !XML_SCHEMAS_DOWNLOADED) {
            contentImportService.importXmlSchemas();
            XML_SCHEMAS_DOWNLOADED = true;
        }
    }

    /**
     * Remove all entities from the database.
     */
    private void teardown(final boolean force) {
        // Only bother to tear down the database if we have run at least one
        // test in this test class.
        if (force || getTestCount() > 1) {
            commonTestControl.teardown();
        }
    }

    private static int getTestCount() {
        final State state = TestState.getState();
        return state.getClassTestCount();
    }

    @Override
    public Path getCurrentTestDir() {
        return FileUtil.getTempDir();
    }
}
