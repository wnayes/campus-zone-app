package wnayes.campuszoneapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

/**
 * REST client library for Java.
 * @author Luke Lowrey
 * See: http://lukencode.com/2010/04/27/calling-web-services-in-android-using-httpclient/
 */
public class RestClient {
    public enum RequestMethod {
        GET, POST, PUT, DELETE
    }

    private String response;
    public String getResponse() {
        return response;
    }

    private String message;
    public String getErrorMessage() {
        return message;
    }

    private int responseCode;
    public int getResponseCode() {
        return responseCode;
    }

    private String url;
    public RestClient(String url) {
        this.url = url;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
    }

    private ArrayList<NameValuePair> params;
    public void AddParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }
    public int getParamCount() {
        return params.size();
    }

    private ArrayList<NameValuePair> headers;
    public void AddHeader(String name, String value) {
        headers.add(new BasicNameValuePair(name, value));
    }

    /**
     * The time in milliseconds we will wait to establish a connection.
     */
    private int connectionTimeout = 0;

    /**
     * The time we will wait to receive data from the server.
     */
    private int socketTimeout = 0;

    /**
     * Sets the connection and socket timeouts for our http requests.
     * @param connectionTimeout
     * @param socketTimeout
     */
    public void SetTimeout(int connectionTimeout, int socketTimeout) {
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
    }

    public void Execute(RequestMethod method) throws Exception {
        switch(method) {
            case GET:
            {
                // Add request parameters
                String combinedParams = "";
                if(!params.isEmpty()) {
                    combinedParams += "?";
                    for (NameValuePair p : params) {
                        String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                        if(combinedParams.length() > 1)
                            combinedParams  +=  "&" + paramString;
                        else
                            combinedParams += paramString;
                    }
                }

                HttpGet request = new HttpGet(url + combinedParams);

                // Add headers to request
                for(NameValuePair h : headers)
                    request.addHeader(h.getName(), h.getValue());

                executeRequest(request, url);
                break;
            }
            case POST:
            {
                HttpPost request = new HttpPost(url);

                // Add headers to request
                for (NameValuePair h : headers)
                    request.addHeader(h.getName(), h.getValue());

                if (!params.isEmpty())
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

                executeRequest(request, url);
                break;
            }
            case PUT:
            {
                HttpPut request = new HttpPut(url);

                // Add headers to request
                for (NameValuePair h : headers)
                    request.addHeader(h.getName(), h.getValue());

                if (!params.isEmpty())
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

                executeRequest(request, url);
                break;
            }
            case DELETE:
            {
                // Add request parameters
                String combinedParams = "";
                if(!params.isEmpty()) {
                    combinedParams += "?";
                    for (NameValuePair p : params) {
                        String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                        if(combinedParams.length() > 1)
                            combinedParams  +=  "&" + paramString;
                        else
                            combinedParams += paramString;
                    }
                }

                HttpDelete request = new HttpDelete(url + combinedParams);

                // Add headers to request
                for (NameValuePair h : headers)
                    request.addHeader(h.getName(), h.getValue());

                executeRequest(request, url);
                break;
            }
        }
    }

    private void executeRequest(HttpUriRequest request, String url) {
        // Configure the request to timeout if necessary.
        HttpParams httpParameters = new BasicHttpParams();
        if (this.connectionTimeout != 0)
            HttpConnectionParams.setConnectionTimeout(httpParameters, this.connectionTimeout);
        if (this.socketTimeout != 0)
            HttpConnectionParams.setSoTimeout(httpParameters, socketTimeout);

        HttpClient client = new DefaultHttpClient(httpParameters);

        HttpResponse httpResponse;
        try {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                response = convertStreamToString(instream);

                // Closing the input stream will trigger connection release
                instream.close();
            }

        } catch (ClientProtocolException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}