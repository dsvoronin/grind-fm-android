package com.dsvoronin.grindfm.util;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.*;

/**
 * User: dsvoronin
 * Date: 27.05.12
 * Time: 17:33
 * HTTP клиент для общения с сервером NPS
 */
public class GrindHttpClient {

    private static final String TAG = "Grind.HttpClient";

    private DefaultHttpClient httpClient;

    /**
     * //Устанавливаем таймауты
     *
     * @param connectionTimeout request
     * @param socketTimeout     response
     */
    public GrindHttpClient(int connectionTimeout, int socketTimeout) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, socketTimeout);

        httpClient = new DefaultHttpClient(params);
    }

    /**
     * Включаем аутентификацию
     *
     * @param login    Login
     * @param password password
     */
    public void setCredentials(String login, String password) {
        Credentials credentials = new UsernamePasswordCredentials(login, password);
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), credentials);
    }

    /**
     * запрос
     *
     * @return string
     */
    public String request(HttpUriRequest httpRequest) throws GrindHttpClientException {
        HttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpRequest);

            if (httpResponse == null) {
                throw new GrindHttpClientException("Нет ответа от сервера");
            }

            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode != 200) {
                throw new GrindHttpClientException("Сообщение от сервера: " + statusLine.toString());
            }

            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity == null) {
                throw new GrindHttpClientException("Пустой ответ от сервера");
            }

            try {
                InputStream is = httpEntity.getContent();
                return convertStreamToString(is);
            } catch (IOException e) {
                throw new GrindHttpClientException("Ошибка при прочтении ответа с сервера", e);
            }

        } catch (IOException e) {
            throw new GrindHttpClientException("Не получается связаться с сервером", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    /**
     * собираем реквест
     *
     * @param url  url
     * @param json тело запроса в json
     * @return request
     */
    public HttpUriRequest buildJsonPost(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-type", "application/json");
        try {
            StringEntity entity = new StringEntity(json);
            entity.setContentType("text/xml");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(entity);
            return httpPost;
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Cant build request", e);
            return null;
        }
    }

    private String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            is.close();
        }
        return sb.toString();
    }
}
