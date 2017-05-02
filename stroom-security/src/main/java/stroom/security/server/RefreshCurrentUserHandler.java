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

package stroom.security.server;

import org.springframework.context.annotation.Scope;
import stroom.security.Insecure;
import stroom.security.shared.RefreshCurrentUserAction;
import stroom.security.shared.User;
import stroom.security.shared.UserAndPermissions;
import stroom.task.server.AbstractTaskHandler;
import stroom.task.server.TaskHandlerBean;
import stroom.util.spring.StroomScope;

import javax.inject.Inject;

@TaskHandlerBean(task = RefreshCurrentUserAction.class)
@Scope(value = StroomScope.TASK)
@Insecure
public class RefreshCurrentUserHandler extends AbstractTaskHandler<RefreshCurrentUserAction, UserAndPermissions> {
    private final AuthenticationService authenticationService;
    private final UserAndPermissionsHelper userAndPermissionsHelper;

    @Inject
    RefreshCurrentUserHandler(final AuthenticationService authenticationService, final UserAndPermissionsHelper userAndPermissionsHelper) {
        this.authenticationService = authenticationService;
        this.userAndPermissionsHelper = userAndPermissionsHelper;
    }

    @Override
    public UserAndPermissions exec(final RefreshCurrentUserAction task) {
        authenticationService.refreshCurrentUser();

        // Re-fetch
        final User user = authenticationService.getCurrentUser();

        return userAndPermissionsHelper.get(user);
    }
}
