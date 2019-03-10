package com.wisesupport.example;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author c00286900
 */
@RestController
@RequestMapping("/cache")
public class CacheController {

    @GetMapping
    public ResponseEntity<String> cache() {
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS))
                .eTag("11111")
                .body("haha");
    }
}
