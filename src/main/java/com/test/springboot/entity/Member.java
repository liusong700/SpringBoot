package com.test.springboot.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class Member implements Serializable {

    private static final long serialVersionUID = 3622959437370574999L;

    private Integer regNo;
    private String nickName;
    private String realName;
}
