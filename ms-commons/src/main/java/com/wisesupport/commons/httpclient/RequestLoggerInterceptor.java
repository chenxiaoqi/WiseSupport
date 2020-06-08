package com.wisesupport.commons.httpclient;

import org.apache.http.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RequestLoggerInterceptor implements HttpResponseInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggerInterceptor.class);

    private static final Set<String> EXCLUDED_HEADERS = new HashSet<>();

    static {
        EXCLUDED_HEADERS.add(HttpHeaders.DATE);
        EXCLUDED_HEADERS.add(HttpHeaders.EXPIRES);
        EXCLUDED_HEADERS.add(HttpHeaders.CONNECTION);
        EXCLUDED_HEADERS.add(HttpHeaders.CACHE_CONTROL);
        EXCLUDED_HEADERS.add(HttpHeaders.TRANSFER_ENCODING);
        EXCLUDED_HEADERS.add(HttpHeaders.PRAGMA);
        EXCLUDED_HEADERS.add(HttpHeaders.SERVER);
        EXCLUDED_HEADERS.add(HttpHeaders.USER_AGENT);
        EXCLUDED_HEADERS.add(HttpHeaders.ACCEPT_ENCODING);
        EXCLUDED_HEADERS.add(HttpHeaders.CONTENT_TYPE);

    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!log.isDebugEnabled()) {
            return;
        }
        synchronized (RequestLoggerInterceptor.class) {
            HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
            log.debug(">>> {}", request.getRequestLine());
            for (Header header : request.getAllHeaders()) {
                if (EXCLUDED_HEADERS.contains(header.getName())) {
                    continue;
                }
                log.debug(">>> {} {}", header.getName(), header.getValue());
            }

            log.debug("<<< {}", response.getStatusLine());
            for (Header header : response.getAllHeaders()) {
                if (EXCLUDED_HEADERS.contains(header.getName())) {
                    continue;
                }
                log.debug("<<< {} {}", header.getName(), header.getValue());
            }
        }
    }
}
