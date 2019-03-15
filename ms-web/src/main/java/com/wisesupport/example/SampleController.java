package com.wisesupport.example;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  * @author c00286900
 *   */

@RestController
@RequestMapping("/sample")
@AllArgsConstructor
public class SampleController {

    private SampleService sampleService;

    @GetMapping("/cache")
    public String cache(int input) {
        return sampleService.expensive(input);
    }
}

