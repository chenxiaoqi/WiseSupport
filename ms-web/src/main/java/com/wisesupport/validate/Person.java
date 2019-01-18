package com.wisesupport.validate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Date;

/**
 * Author chenxiaoqi on 2018/12/23.
 */
@Getter
@Setter
@ToString
@ApiModel(description = "model Person")
public class Person {

    @Size(min = 1, max = 20)
    @NotNull
    private String name;

    @Size(min = 1, max = 20)
    private String address;

    @Min(1)
    @Max(150)
    @ApiModelProperty(value = "年龄",example = "39")
    private int age;


    private Date birthDate;
}
