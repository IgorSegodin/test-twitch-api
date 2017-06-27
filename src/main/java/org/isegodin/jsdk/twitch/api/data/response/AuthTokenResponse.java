package org.isegodin.jsdk.twitch.api.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.isegodin.jsdk.twitch.api.data.dict.Scope;

import java.util.List;

/**
 * @author isegodin
 */
public class AuthTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("scope")
    private List<Scope> scopes;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }
}
