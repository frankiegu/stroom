package stroom.security;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import stroom.logging.AuthenticationEventLog;
import stroom.security.shared.UserRef;
import stroom.servlet.SessionListListener;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is used by the OpenId flow - when a user request a log out the remote authentication service
 * needs to ask all relying parties to log out. This is the back-channel resource that allows this to
 * happen.
 */
@Api(value = "session - /v1")
@Path("/session/v1")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SessionResource.class);

    private final AuthenticationEventLog eventLog;

    @Inject
    SessionResource(final AuthenticationEventLog eventLog) {
        this.eventLog = eventLog;
    }

    @GET
    @Path("logout/{sessionId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Logs the specified session out of Stroom",
            response = Response.class)
    public Response logout(@PathParam("sessionId") String authSessionId) {
        LOGGER.info("Logging out session {}", authSessionId);

        // TODO : We need to lookup the auth session in our user sessions

        final HttpSession session = SessionListListener.getSession(authSessionId);
        final UserRef userRef = UserRefSessionUtil.get(session);
        if (session != null) {
            // Invalidate the current user session
            session.invalidate();
        }
        if (userRef != null) {
            // Create an event for logout
            eventLog.logoff(userRef.getName());
        }

        return Response.status(Response.Status.OK).entity("Logout successful").build();
    }
}
