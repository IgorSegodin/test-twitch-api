package org.isegodin.jsdk.twitch.api.service.auth;

import org.isegodin.jsdk.twitch.api.data.dict.Scope;

import java.util.List;

/**
 * Holds auth data, for further usage and persistence.
 *
 * @author isegodin
 */
public class AuthInfo {

    private volatile String token;
    private volatile String refreshToken;
    private volatile List<Scope> scopes;

    public AuthInfo() {
    }

    public AuthInfo(String token, String refreshToken, List<Scope> scopes) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.scopes = scopes;
    }

    public boolean isAuthenticated() {
        return token != null;
    }

    public void update(String token, String refreshToken, List<Scope> scopes) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.scopes = scopes;
    }

    public void reset() {
        token = null;
        refreshToken = null;
        scopes = null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
