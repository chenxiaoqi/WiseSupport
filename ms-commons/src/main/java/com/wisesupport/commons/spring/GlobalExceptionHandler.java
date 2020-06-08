package com.wisesupport.commons.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler
    public String handler(Throwable throwable, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("handle {} failed", request.getRequestURI(), throwable);
        response.sendError(500);
        return throwable.getMessage();
    }

}
