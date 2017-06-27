package org.isegodin.jsdk.twitch.api.service.auth;

import org.isegodin.jsdk.twitch.api.data.response.AuthTokenResponse;

/**
 * @author isegodin
 */
public interface AuthService {

    AuthTokenResponse authenticate();
}
