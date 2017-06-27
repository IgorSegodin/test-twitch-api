package org.isegodin.jsdk.twitch.api.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author isegodin
 */
public class HttpUtils {

    public static String get(String url, Map<String, String> headers) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    con.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            int responseCode = con.getResponseCode();
            // TODO handle response error code

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String post(String url, String urlParams) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();

            con.setRequestMethod("POST");

            if (urlParams != null) {
                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(urlParams);
                wr.flush();
                wr.close();
            }

            int responseCode = con.getResponseCode();
            // TODO handle response error code

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            return response.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeQueryParam(String source) {
        try {
            return URLEncoder.encode(source, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
