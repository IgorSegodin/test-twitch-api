package org.isegodin.jsdk.twitch.api.service;

import org.isegodin.jsdk.twitch.api.core.TwitchConfig;
import org.isegodin.jsdk.twitch.api.service.auth.AuthInfo;
import org.isegodin.jsdk.twitch.api.data.response.AuthTokenResponse;
import org.isegodin.jsdk.twitch.api.data.response.SubscriptionResponse;
import org.isegodin.jsdk.twitch.api.service.auth.AuthService;
import org.isegodin.jsdk.twitch.api.service.auth.AuthServiceImpl;
import org.isegodin.jsdk.twitch.api.util.HttpUtils;

import java.util.HashMap;
import java.util.function.Function;

/**
 * @author isegodin
 */
public class TwitchApiServiceImpl implements TwitchApiService {

    private final Object monitor = new Object();

    private final AuthInfo authInfo = new AuthInfo();

    private final TwitchConfig twitchConfig;

    private final AuthService authService;

    public TwitchApiServiceImpl(TwitchConfig twitchConfig) {
        this.twitchConfig = twitchConfig;
        this.authService = new AuthServiceImpl(twitchConfig);
    }

    @Override
    public String getAccessToken() {
        return ensureAuthenticated(AuthInfo::getToken);
    }

    @Override
    public SubscriptionResponse getSubscriptions() {
        return ensureAuthenticated(authInfo -> {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/vnd.twitchtv.v5+json");
            headers.put("Client-ID", twitchConfig.getClientId());
            headers.put("Authorization", "OAuth " + authInfo.getToken());

            String response = HttpUtils.get("https://api.twitch.tv/kraken/streams/followed", headers);

            // TODO convert to SubscriptionResponse

            return new SubscriptionResponse();
        });
    }

    protected <RESPONSE> RESPONSE ensureAuthenticated(Function<AuthInfo, RESPONSE> callback) {
        if (authInfo.isAuthenticated()) {
            return callback.apply(authInfo);
        }
        synchronized (monitor) {
            if (authInfo.isAuthenticated()) {
                return callback.apply(authInfo);
            }
            AuthTokenResponse authTokenResponse = authService.authenticate();

            authInfo.update(authTokenResponse.getAccessToken(), authTokenResponse.getRefreshToken(), authTokenResponse.getScopes());

            return callback.apply(authInfo);
        }
    }
}
