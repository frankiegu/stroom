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

package stroom.feed;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import stroom.entity.CachingEntityManager;
import stroom.entity.FindService;
import stroom.explorer.ExplorerActionHandler;
import stroom.feed.shared.FeedDoc;
import stroom.importexport.ImportExportActionHandler;
import stroom.importexport.ImportExportHelper;
import stroom.pipeline.PipelineStore;
import stroom.pipeline.PipelineStoreImpl;
import stroom.security.SecurityContext;
import stroom.persist.EntityManagerSupport;
import stroom.streamstore.FdService;
import stroom.streamstore.FdServiceImpl;
import stroom.task.TaskHandler;

import javax.inject.Named;

public class FeedModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FdService.class).to(FdServiceImpl.class);
        bind(FeedStore.class).to(FeedStoreImpl.class);
        bind(FeedNameCache.class).to(FeedNameCacheImpl.class);
        bind(RemoteFeedService.class).annotatedWith(Names.named("remoteFeedService")).to(RemoteFeedServiceImpl.class);

        // TODO : @66 FIX PLACES THAT USE PIPELINE CACHING
        bind(FeedStore.class).annotatedWith(Names.named("cachedFeedStore")).to(FeedStoreImpl.class);

        final Multibinder<TaskHandler> taskHandlerBinder = Multibinder.newSetBinder(binder(), TaskHandler.class);
        taskHandlerBinder.addBinding().to(stroom.feed.FetchSupportedEncodingsActionHandler.class);

        final Multibinder<ExplorerActionHandler> explorerActionHandlerBinder = Multibinder.newSetBinder(binder(), ExplorerActionHandler.class);
        explorerActionHandlerBinder.addBinding().to(FeedStoreImpl.class);

        final Multibinder<ImportExportActionHandler> importExportActionHandlerBinder = Multibinder.newSetBinder(binder(), ImportExportActionHandler.class);
        importExportActionHandlerBinder.addBinding().to(FeedStoreImpl.class);

        final MapBinder<String, Object> entityServiceByTypeBinder = MapBinder.newMapBinder(binder(), String.class, Object.class);
        entityServiceByTypeBinder.addBinding(FeedDoc.DOCUMENT_TYPE).to(FdServiceImpl.class);

        final Multibinder<FindService> findServiceBinder = Multibinder.newSetBinder(binder(), FindService.class);
        findServiceBinder.addBinding().to(FdServiceImpl.class);
    }
//
//    @Provides
//    @Named("cachedFeedService")
//    public FdService cachedFeedService(final CachingEntityManager entityManager,
//                                       final EntityManagerSupport entityManagerSupport,
//                                       final ImportExportHelper importExportHelper,
//                                       final SecurityContext securityContext) {
//        return new FdServiceImpl(entityManager, entityManagerSupport, importExportHelper, securityContext);
//    }
}