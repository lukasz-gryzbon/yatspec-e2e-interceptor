package com.something.config.interceptor;

import com.something.config.captor.http.RequestCaptor;
import com.something.config.captor.http.ResponseCaptor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created to intercept rest template calls for Yatspec interactions.
 * Attempts to reset the input stream so that no data is lost on reading the response body
 */
@RequiredArgsConstructor
public class LsdRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final RequestCaptor requestCaptor;
    private final ResponseCaptor responseCaptor;

    // TODO How do we make it conditional?
    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        requestCaptor.captureRequestInteraction(request, new String(body));
        final ClientHttpResponse response = execution.execute(request, body);
        responseCaptor.captureResponseInteraction(response, request.getURI().getPath());
        return response;
    }
}