package com.yatspec.e2e.captor.http.template;

/**
 * Useful to keep the interaction messages consistent across various http interceptors.
 */
public class HttpInteractionMessageTemplates {

    public static final String REQUEST_TEMPLATE = "%s %s from %s to %s";
    public static final String RESPONSE_TEMPLATE = "%s response from %s to %s";

    public static String requestOf(final String method, final String path, final String sourceName, final String destinationName) {
        return String.format(REQUEST_TEMPLATE, method, path, sourceName, destinationName);
    }

    public static String responseOf(final String message, final String destinationName, final String sourceName) {
        return String.format(RESPONSE_TEMPLATE, message, destinationName, sourceName);
    }
}