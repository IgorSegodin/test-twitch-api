package org.isegodin.jsdk.twitch.api.core;

import org.isegodin.jsdk.twitch.api.data.dict.Scope;

import java.util.List;

/**
 * @author isegodin
 */
public class TwitchConfig {

    private volatile String apiUrl = "https://api.twitch.tv/kraken";
    private volatile String clientId;
    private volatile String clientSecret;
    private volatile String redirectUrl;
    private volatile List<Scope> scopes;

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }
}
