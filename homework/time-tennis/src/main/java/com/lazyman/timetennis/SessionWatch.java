package com.lazyman.timetennis;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebListener
@Slf4j
public class SessionWatch implements HttpSessionListener {

    private static Map<String, HttpSession> OPEN_ID_SESSION_ID_MAPPING = new ConcurrentHashMap<>();

    @Override
    public void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        log.info("session destroyed {}.", OPEN_ID_SESSION_ID_MAPPING.entrySet().removeIf(entry -> entry.getValue().getId().equals(sessionId)));
    }

    public static void register(String openId, HttpSession session) {
        OPEN_ID_SESSION_ID_MAPPING.put(openId, session);
        log.info("session count {}",OPEN_ID_SESSION_ID_MAPPING.size());
    }

    public static void destroy(String openId) {
        HttpSession session = OPEN_ID_SESSION_ID_MAPPING.get(openId);
        if (session != null) {
            session.invalidate();
        }
    }
}
