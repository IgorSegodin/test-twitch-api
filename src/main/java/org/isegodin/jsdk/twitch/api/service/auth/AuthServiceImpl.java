package org.isegodin.jsdk.twitch.api.service.auth;


import io.undertow.Undertow;
import io.undertow.util.Headers;
import org.isegodin.jsdk.twitch.api.core.TwitchConfig;
import org.isegodin.jsdk.twitch.api.data.response.AuthTokenResponse;
import org.isegodin.jsdk.twitch.api.util.HttpUtils;
import org.isegodin.jsdk.twitch.api.util.JsonService;
import org.isegodin.jsdk.twitch.api.util.RandomUtil;
import org.isegodin.jsdk.twitch.api.util.Stopwatch;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.Deque;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author isegodin
 */
public class AuthServiceImpl implements AuthService {

    private final TwitchConfig twitchConfig;

    private JsonService jsonService = new JsonService();

    public AuthServiceImpl(TwitchConfig twitchConfig) {
        this.twitchConfig = twitchConfig;
    }

    public AuthTokenResponse authenticate() {
        final ResponseHolder responseHolder = new ResponseHolder();

        Undertow server = Undertow.builder()
                .addHttpListener(resolveRedirectPort(), "localhost")
                .setHandler(exchange -> {
                    Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
                    if (queryParameters.containsKey("code")) {
                        String accessCode = queryParameters.get("code").getFirst();

                        responseHolder.setResponse(requestAuth(accessCode));
                        synchronized (AuthServiceImpl.this) {
                            AuthServiceImpl.this.notify();
                        }

                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                        exchange.getResponseSender().send(
                                ByteBuffer.wrap(getResourceBytes("html/access_code.html"))
                        );
                    } else {
                        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("OK");
                    }
                })
                .build();

        server.start();

        openUrl(buildAccessCodeUrl());

        synchronized (this) {

            Stopwatch stopwatch = Stopwatch.start();
            try {
                wait(60_000);
            } catch (InterruptedException e) {
                //
            }
            if (stopwatch.isElapsedMillis(60_000)) {
                throw new RuntimeException("Authentication timeout, application did not receive response from twitch.");
            }
        }

        server.stop();

        return responseHolder.getResponse();
    }

    private String buildAccessCodeUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(twitchConfig.getApiUrl() + "/oauth2/authorize")
                .append("?response_type=code")
                .append("&client_id=").append(twitchConfig.getClientId())
                .append("&redirect_uri=").append(HttpUtils.encodeQueryParam(twitchConfig.getRedirectUrl()))
                .append("&scope=").append(HttpUtils.encodeQueryParam(twitchConfig.getScopes().stream().map(Enum::name).collect(Collectors.joining(" "))))
                .append("&state=").append(RandomUtil.nextAlphanumeric());
        return sb.toString();
    }

    private AuthTokenResponse requestAuth(String accessCode) {
        StringBuilder sb = new StringBuilder()
                .append("client_id=").append(twitchConfig.getClientId())
                .append("&client_secret=").append(HttpUtils.encodeQueryParam(twitchConfig.getClientSecret()))
                .append("&grant_type=authorization_code")
                .append("&redirect_uri=").append(HttpUtils.encodeQueryParam(twitchConfig.getRedirectUrl()))
                .append("&code=").append(accessCode)
                .append("&state=").append(RandomUtil.nextAlphanumeric());
        String response = HttpUtils.post(twitchConfig.getApiUrl() + "/oauth2/token", sb.toString());
        return jsonService.fromJson(response, AuthTokenResponse.class);
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Can not open browser.");
    }

    private byte[] getResourceBytes(String path) {
        InputStream resourceStream = this.getClass().getClassLoader().getResourceAsStream(path);
        if (resourceStream == null) {
            throw new RuntimeException("Resource not found: " + path);
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[2048];
        int read;

        try (InputStream is = resourceStream) {
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return os.toByteArray();
    }

    private int resolveRedirectPort() {
        Matcher matcher = Pattern.compile("^(?<protocol>\\S+://)?(?<domain>[^:/]+):(?<port>\\d+).*$")
                .matcher(twitchConfig.getRedirectUrl());

        if (!matcher.matches()) {
            throw new RuntimeException("Can not resolve port from redirect URL: " + twitchConfig.getRedirectUrl());
        }

        return Integer.valueOf(matcher.group("port"));
    }

    private static class ResponseHolder {

        private volatile AuthTokenResponse response;

        public AuthTokenResponse getResponse() {
            return response;
        }

        public void setResponse(AuthTokenResponse response) {
            this.response = response;
        }
    }

}
