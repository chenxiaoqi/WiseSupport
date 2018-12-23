package com.wisesupport.validate;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Author chenxiaoqi on 2018/12/22.
 */
@RestController
@RequestMapping("/validate")
@Validated
public class ValidatorController {

    @GetMapping(value = "/binding", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Person binding(@Valid Person person) {

        return person;
    }

    @GetMapping(value = "/aop", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String aop(@NotNull @Size(min = 1, max = 10) String name) {
        return name;
    }


}
