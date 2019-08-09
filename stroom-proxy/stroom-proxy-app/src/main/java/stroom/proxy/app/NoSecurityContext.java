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

package stroom.proxy.app;

import stroom.security.api.SecurityContext;
import stroom.security.shared.UserToken;

import java.util.function.Supplier;

public class NoSecurityContext implements SecurityContext {
    @Override
    public String getUserId() {
        return "admin";
    }

    @Override
    public UserToken getUserToken() {
        return null;
    }

    @Override
    public String getApiToken() {
        return null;
    }

    @Override
    public boolean isLoggedIn() {
        return true;
    }

    @Override
    public boolean isAdmin() {
        return true;
    }

    @Override
    public boolean hasAppPermission(final String permission) {
        return true;
    }

    @Override
    public boolean hasDocumentPermission(final String documentType, final String documentId, final String permission) {
        return true;
    }

    @Override
    public <T> T asUserResult(final UserToken userToken, final Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public void asUser(final UserToken userToken, final Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T> T asProcessingUserResult(final Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public void asProcessingUser(final Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T> T useAsReadResult(final Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public void useAsRead(final Runnable runnable) {
        runnable.run();
    }

    @Override
    public void secure(final String permission, final Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T> T secureResult(final String permission, final Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public void secure(final Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T> T secureResult(final Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public void insecure(final Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T> T insecureResult(final Supplier<T> supplier) {
        return supplier.get();
    }
}
