package com.wisesupport.commons.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler
    @ResponseBody
    public Map<String, String> handler(Throwable throwable, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("handle {} failed", request.getRequestURI(), throwable);
        if (throwable instanceof ServletRequestBindingException && throwable.getMessage().startsWith("Missing session attribute")) {
            response.setStatus(401);
        }else {
            response.setStatus(500);
        }
        Map<String, String> result = new HashMap<>(1);
        result.put("code", "ServerError");
        result.put("error", throwable.getMessage());
        return result;
    }

}
