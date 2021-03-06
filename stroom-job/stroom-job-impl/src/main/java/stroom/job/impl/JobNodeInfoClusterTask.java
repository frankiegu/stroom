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

package stroom.job.impl;

import stroom.cluster.task.api.ClusterTask;
import stroom.job.shared.JobNode;
import stroom.job.shared.JobNodeInfo;
import stroom.security.shared.UserToken;
import stroom.util.shared.SharedMap;

class JobNodeInfoClusterTask extends ClusterTask<SharedMap<JobNode, JobNodeInfo>> {
    private static final long serialVersionUID = 3242415690833883484L;

    JobNodeInfoClusterTask(final UserToken userToken) {
        super(userToken, "JobNodeInfoClusterTask");
    }
}
