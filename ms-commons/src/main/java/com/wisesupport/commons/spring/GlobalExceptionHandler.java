package com.wisesupport.commons.spring;

import com.wisesupport.commons.exceptions.BusinessException;
import com.wisesupport.commons.exceptions.LoginTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
        if (throwable instanceof LoginTimeoutException) {
            response.setStatus(401);
            log.debug("handle {} failed", request.getRequestURI(), throwable);
        } else {
            if (throwable instanceof BusinessException || throwable instanceof MethodArgumentTypeMismatchException || throwable instanceof ServletRequestBindingException) {
                log.debug("handle {} failed", request.getRequestURI(), throwable);
            } else {
                log.error("handle {} failed", request.getRequestURI(), throwable);
            }
            response.setStatus(500);
        }
        Map<String, String> result = new HashMap<>(1);
        result.put("code", "ServerError");
        result.put("error", throwable.getMessage());
        return result;
    }

}
