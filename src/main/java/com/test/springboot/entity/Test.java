package com.test.springboot.entity;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Min;
import java.util.List;

@Data
@ToString
public class Test {

    private List<String> names;
    @Min(value = 1, message = "体验天数不能小于1天")
    private Integer experienceDay;

}
