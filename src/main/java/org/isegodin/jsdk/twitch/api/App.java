package org.isegodin.jsdk.twitch.api;

import org.isegodin.jsdk.twitch.api.core.TwitchConfig;
import org.isegodin.jsdk.twitch.api.data.dict.Scope;
import org.isegodin.jsdk.twitch.api.data.response.SubscriptionResponse;
import org.isegodin.jsdk.twitch.api.service.TwitchApiService;
import org.isegodin.jsdk.twitch.api.service.TwitchApiServiceImpl;
import org.isegodin.jsdk.twitch.api.service.chat.TwitchChat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author isegodin
 */
public class App {

    public static void main(String[] args) throws IOException {
        Properties props = new Properties();
        props.load(App.class.getClassLoader().getResourceAsStream("application.properties"));

        TwitchConfig config = new TwitchConfig();
        config.setClientId(props.getProperty("twitch.client-id"));
        config.setClientSecret(props.getProperty("twitch.client-secret"));
        config.setRedirectUrl(props.getProperty("twitch.redirect-url"));
        config.setScopes(Arrays.asList(Scope.chat_login, Scope.user_read, Scope.user_subscriptions));

        TwitchApiService twitchApiService = new TwitchApiServiceImpl(config);

        SubscriptionResponse subscriptions = twitchApiService.getSubscriptions();

        TwitchChat chat = new TwitchChat("lastzerg", twitchApiService.getAccessToken());

        chat.joinChannel("idmeety");

//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
//
//        while (true) {
//
//        }
    }

}
