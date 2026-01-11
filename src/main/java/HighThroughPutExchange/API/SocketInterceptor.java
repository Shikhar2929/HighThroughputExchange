package HighThroughPutExchange.API;

import HighThroughPutExchange.API.api_objects.requests.BasePrivateRequest;
import HighThroughPutExchange.API.authentication.PrivatePageAuthenticator;
import java.net.URI;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SocketInterceptor implements HandshakeInterceptor {

    private final PrivatePageAuthenticator privatePageAuthenticator;

    public SocketInterceptor(PrivatePageAuthenticator privatePageAuthenticator) {
        this.privatePageAuthenticator = privatePageAuthenticator;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        // Use the sessions bean to validate the session ID
        URI uri = request.getURI();
        Map<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams().toSingleValueMap();
        String sessionId = queryParams.get("Session-ID");
        String username = queryParams.get("Username");
        if (!privatePageAuthenticator.authenticate(new BasePrivateRequest(username, sessionId))) {
            return false;
        }
        Principal userPrincipal = new UserPrincipal(username);
        attributes.put("principal", userPrincipal);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }

    private static class UserPrincipal implements Principal {
        private final String username;

        public UserPrincipal(String username) {
            this.username = username;
        }

        @Override
        public String getName() {
            return username;
        }

        @Override
        public String toString() {
            return username; // Useful for debugging and logging
        }
    }
}
