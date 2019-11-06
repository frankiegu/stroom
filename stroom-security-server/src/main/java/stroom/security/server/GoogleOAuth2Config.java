package stroom.security.server;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleOAuth2Config {
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";

    private String authUrl = AUTH_URL;
    private String tokenUrl = TOKEN_URL;
    private String clientId;
    private String clientSecret;
    private String redirectUri;

    @JsonProperty
    public String getAuthUrl() {
        return authUrl;
    }

    @JsonProperty
    public void setAuthUrl(final String authUrl) {
        this.authUrl = authUrl;
    }

    @JsonProperty
    public String getTokenUrl() {
        return tokenUrl;
    }

    @JsonProperty
    public void setTokenUrl(final String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    @JsonProperty
    public String getClientId() {
        return clientId;
    }

    @JsonProperty
    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    @JsonProperty
    public String getClientSecret() {
        return clientSecret;
    }

    @JsonProperty
    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @JsonProperty
    public String getRedirectUri() {
        return redirectUri;
    }

    @JsonProperty
    public void setRedirectUri(final String redirectUri) {
        this.redirectUri = redirectUri;
    }
}
