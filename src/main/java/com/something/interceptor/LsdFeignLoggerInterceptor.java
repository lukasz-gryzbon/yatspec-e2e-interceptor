package com.something.interceptor;

import com.something.captor.http.RequestCaptor;
import com.something.captor.http.ResponseCaptor;
import feign.Logger;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class LsdFeignLoggerInterceptor extends Logger.JavaLogger {

    private final RequestCaptor requestCaptor;
    private final ResponseCaptor responseCaptor;

    public LsdFeignLoggerInterceptor(final RequestCaptor requestCaptor, final ResponseCaptor responseCaptor) {
        super(LsdFeignLoggerInterceptor.class);
        this.requestCaptor = requestCaptor;
        this.responseCaptor = responseCaptor;
    }

    @Override
    protected void logRequest(final String configKey, final Level level, final Request request) {
        super.logRequest(configKey, level, request);
        requestCaptor.captureRequestInteraction(request);
    }

    @Override
    protected Response logAndRebufferResponse(final String configKey, final Level logLevel, final Response response, final long elapsedTime) throws IOException {
        super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
        final Map<String, Object> data = responseCaptor.captureResponseInteraction(response);
        return resetBodyData(response, ((String)data.get("body")).getBytes());
    }

    private Response resetBodyData(final Response response, final byte[] bodyData) {
        return response.toBuilder().body(bodyData).build();
    }
}