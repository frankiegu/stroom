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

package stroom.task.server;

import org.springframework.context.annotation.Scope;
import stroom.entity.shared.ResultList;
import stroom.security.Secured;
import stroom.task.cluster.ClusterDispatchAsyncHelper;
import stroom.task.shared.FindTaskProgressAction;
import stroom.task.shared.TaskProgress;
import stroom.util.spring.StroomScope;

import javax.inject.Inject;

@TaskHandlerBean(task = FindTaskProgressAction.class)
@Scope(StroomScope.TASK)
@Secured(FindTaskProgressAction.MANAGE_TASKS_PERMISSION)
class FindTaskProgressHandler
        extends FindTaskProgressHandlerBase<FindTaskProgressAction, ResultList<TaskProgress>> {
    @Inject
    FindTaskProgressHandler(final ClusterDispatchAsyncHelper dispatchHelper) {
        super(dispatchHelper);
    }

    @Override
    public ResultList<TaskProgress> exec(final FindTaskProgressAction action) {
        return doExec(action, action.getCriteria());
    }
}
