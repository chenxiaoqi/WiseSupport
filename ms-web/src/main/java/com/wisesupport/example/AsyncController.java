package com.wisesupport.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

/**
 * @author c00286900
 */
@RestController
@RequestMapping("/async")
public class AsyncController {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncController.class);
    private ThreadPoolTaskExecutor executor;

    public AsyncController(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    @GetMapping(value = "/deferred", produces = {MediaType.APPLICATION_JSON_VALUE})
    public DeferredResult<String> async() {
        DeferredResult<String> result = new DeferredResult<>();
        executor.execute(() -> result.setResult("async request"));
        return result;
    }

    @GetMapping(value = "/callable", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Callable<String> callable() {
        return () -> "callable request";
    }

    @GetMapping("/events")
    public ResponseEntity<ResponseBodyEmitter> events() {
        //也可以直接返回
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        executor.execute(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    emitter.send("hello " + i + "\n");
                    Thread.sleep(1000);
                }
            }
            catch (IOException | InterruptedException e) {
                LOG.error("events failed.", e);
            }
            emitter.complete();
        });
        return ResponseEntity.ok().body(emitter);
    }

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sse() {
        SseEmitter sse = new SseEmitter();
        executor.execute(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    sse.send("hello " + i);
                    Thread.sleep(1000);
                }
            }
            catch (IOException | InterruptedException e) {
                LOG.error("sse failed.", e);
            }
            sse.complete();
        });
        return sse;
    }

    @GetMapping("/download")
    public StreamingResponseBody handle() {
        return outputStream -> outputStream.write("stream body".getBytes(StandardCharsets.UTF_8));
    }
}

