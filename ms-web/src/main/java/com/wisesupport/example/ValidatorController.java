package com.wisesupport.example;

import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 *  * * * Author chenxiaoqi on 2018/12/22.
 *   */
@RestController
@RequestMapping("/validate")
@Validated
@AllArgsConstructor
@Api(tags = "Java Validator Demo", description = "demonstrate how to use java validator")
public class ValidatorController {

    @GetMapping(value = "/binding")
    @ApiOperation(value = "operation-binding", notes = "notes-参数绑定到对象")
    public Person binding(@Valid Person person) {
        return person;
    }

    @GetMapping(value = "/aop")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "name", defaultValue = "cxq")
            }
    )
    @ApiResponse(code = 200, message = "ok")
    public String aop(@NotNull @Size(min = 1, max = 10) @RequestParam String name,
                      @NotNull @Size(max = 2) @RequestParam List<@Digits(integer = 2, fraction = 0) String> habits,
                      @NotNull @Id @RequestParam String id

    ) {
        return name + habits + id;
    }


    @PostMapping("/json")
    public Person json(@RequestBody Person person) {
        return person;
    }

    @GetMapping(value = "/date")
    @Timed("validate.date")
    public Date date(@DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss") @RequestParam Date date) {
        return date;
    }

}



