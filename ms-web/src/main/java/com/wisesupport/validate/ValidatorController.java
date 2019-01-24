package com.wisesupport.validate;

import com.wisesupport.user.UserMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *  * Author chenxiaoqi on 2018/12/22.
 *   */
@RestController
@RequestMapping("/validate")
@Validated
@AllArgsConstructor
@Api(tags = "验证", description = "验证DEMO")
@Secured("ROLE_USER")
public class ValidatorController {

    private UserMapper userMapper;

    @GetMapping(value = "/binding", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiOperation(value = "operation-binding", notes = "notes-参数绑定到对象")
    public Person binding(@Valid Person person) {
        return person;
    }

    @GetMapping(value = "/aop", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "name",defaultValue = "cxq")
            }
    )
    @ApiResponse(code = 200, message = "ok")
    public String aop(@NotNull @Size(min = 1, max = 10) String name) {
        return name;
    }


}

