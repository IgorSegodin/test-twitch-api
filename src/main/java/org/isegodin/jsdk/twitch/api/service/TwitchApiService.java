package org.isegodin.jsdk.twitch.api.service;

import org.isegodin.jsdk.twitch.api.data.response.SubscriptionResponse;

/**
 * @author isegodin
 */
public interface TwitchApiService {

    String getAccessToken();

    SubscriptionResponse getSubscriptions();
}
