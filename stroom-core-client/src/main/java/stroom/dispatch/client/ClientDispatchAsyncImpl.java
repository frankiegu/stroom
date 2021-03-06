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

package stroom.dispatch.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.client.rpc.XsrfTokenServiceAsync;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import stroom.alert.client.event.AlertEvent;
import stroom.dispatch.shared.DispatchServiceAsync;
import stroom.docref.SharedObject;
import stroom.task.client.TaskEndEvent;
import stroom.task.client.TaskStartEvent;
import stroom.task.shared.Action;
import stroom.util.client.RandomId;
import stroom.widget.util.client.Future;
import stroom.widget.util.client.FutureImpl;

public class ClientDispatchAsyncImpl implements ClientDispatchAsync, HasHandlers {
    private static final String LOGIN_HTML = "<title>Login</title>";

    private final EventBus eventBus;
    private final DispatchServiceAsync dispatchService;
    private final XsrfTokenServiceAsync xsrf;
    private final String applicationInstanceId;

    private XsrfToken xsrfToken;

    @Inject
    public ClientDispatchAsyncImpl(final EventBus eventBus,
                                   final DispatchServiceAsync dispatchService,
                                   final XsrfTokenServiceAsync xsrf) {
        this.eventBus = eventBus;
        this.dispatchService = dispatchService;
        this.xsrf = xsrf;
        this.applicationInstanceId = RandomId.createDiscrimiator();

        ((ServiceDefTarget) dispatchService).setServiceEntryPoint(GWT.getHostPageBaseURL() + "dispatch.rpc");
        ((ServiceDefTarget) xsrf).setServiceEntryPoint(GWT.getHostPageBaseURL() + "xsrf");
    }

    @Override
    public <R extends SharedObject> Future<R> exec(final Action<R> task) {
        return exec(task, null, true);
    }

    @Override
    public <R extends SharedObject> Future<R> exec(final Action<R> task, final String message) {
        return exec(task, message, true);
    }

    @Override
    public <R extends SharedObject> Future<R> exec(final Action<R> task, final boolean showWorking) {
        return exec(task, null, showWorking);
    }

    private <R extends SharedObject> Future<R> exec(final Action<R> task, final String message, final boolean showWorking) {
        if (showWorking) {
            // Add the task to the map.
            incrementTaskCount(message);
        }

        return dispatch(task, message, showWorking);
    }

    private <R extends SharedObject> Future<R> dispatch(final Action<R> action, final String message,
                                                        final boolean showWorking) {
        action.setApplicationInstanceId(applicationInstanceId);

        final FutureImpl<R> future = new FutureImpl<>();
        // Set the default behaviour of the future to show an error.
        future.onFailure(throwable -> AlertEvent.fireErrorFromException(ClientDispatchAsyncImpl.this, throwable, null));

        Scheduler.get().scheduleDeferred(() -> getXSRFToken(action, message, showWorking, future));

        return future;
    }

    private <R extends SharedObject> void getXSRFToken(final Action<R> action,
                                                       final String message,
                                                       final boolean showWorking,
                                                       final FutureImpl<R> future) {
        if (xsrfToken != null) {
            ((HasRpcToken) dispatchService).setRpcToken(xsrfToken);
            ((HasRpcToken) dispatchService).setRpcTokenExceptionHandler(e ->
                    AlertEvent.fireError(ClientDispatchAsyncImpl.this, e.getMessage(), null));
            dispatchAsync(action, message, showWorking, future);

        } else {
            xsrf.getNewXsrfToken(new AsyncCallback<XsrfToken>() {
                public void onSuccess(XsrfToken token) {
                    xsrfToken = token;

                    // make XSRF protected RPC call
                    ((HasRpcToken) dispatchService).setRpcToken(xsrfToken);
                    ((HasRpcToken) dispatchService).setRpcTokenExceptionHandler(e ->
                            AlertEvent.fireError(ClientDispatchAsyncImpl.this, e.getMessage(), null));
                    dispatchAsync(action, message, showWorking, future);
                }

                public void onFailure(Throwable caught) {
                    AlertEvent.fireError(ClientDispatchAsyncImpl.this, caught.getMessage(), null);

                    try {
                        throw caught;
                    } catch (RpcTokenException e) {
                        // Can be thrown for several reasons:
                        //   - duplicate session cookie, which may be a sign of a cookie
                        //     overwrite attack
                        //   - XSRF token cannot be generated because session cookie isn't
                        //     present
                    } catch (Throwable e) {
                        // unexpected
                    }
                }
            });
        }
    }

    private <R extends SharedObject> void dispatchAsync(final Action<R> action,
                                                        final String message,
                                                        final boolean showWorking,
                                                        final FutureImpl<R> future) {
        dispatchService.exec(action, new AsyncCallback<R>() {
            @Override
            public void onSuccess(final R result) {
                if (showWorking) {
                    // Remove the task from the task count.
                    decrementTaskCount();
                }

                // Let the callback handle success.
                handleSuccess(result);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                if (showWorking) {
                    // Remove the task from the task count.
                    decrementTaskCount();
                }

                if (message != null && message.length() >= LOGIN_HTML.length() && message.contains(LOGIN_HTML)) {
                    if (!("Logout".equalsIgnoreCase(action.getTaskName()))) {
                        // Logout.
                        AlertEvent.fireError(ClientDispatchAsyncImpl.this,
                                "Your user session appears to have terminated", message, null);
                    }
                } else if (throwable instanceof StatusCodeException) {
                    final StatusCodeException scEx = (StatusCodeException) throwable;
                    if (scEx.getStatusCode() >= 100) {
                        if (!("Logout".equalsIgnoreCase(action.getTaskName()))) {
                            // Logout.
                            AlertEvent.fireError(ClientDispatchAsyncImpl.this, "An error has occurred",
                                    scEx.getStatusCode() + " - " + scEx.getMessage(), null);
                        }
                    }
                }

                handleFailure(throwable);
            }

            private void handleSuccess(final R result) {
                try {
                    future.setResult(result);
                } catch (final RuntimeException e) {
                    AlertEvent.fireErrorFromException(ClientDispatchAsyncImpl.this, e, null);
                }
            }

            private void handleFailure(final Throwable throwable) {
                try {
                    future.setThrowable(throwable);
                } catch (final RuntimeException e) {
                    AlertEvent.fireErrorFromException(ClientDispatchAsyncImpl.this, e, null);
                }
            }
        });
    }

    private void incrementTaskCount(final String message) {
        // Add the task to the map.
        TaskStartEvent.fire(ClientDispatchAsyncImpl.this, message);
    }

    private void decrementTaskCount() {
        // Remove the task from the task count.
        TaskEndEvent.fire(ClientDispatchAsyncImpl.this);
    }

    @Override
    public String getImportFileURL() {
        return GWT.getHostPageBaseURL() + "importfile.rpc";
    }

    @Override
    public void fireEvent(final GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }
}
