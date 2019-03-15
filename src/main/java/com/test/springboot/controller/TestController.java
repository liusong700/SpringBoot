package com.test.springboot.controller;

import com.alibaba.fastjson.JSONArray;
import com.test.springboot.entity.Member;
import com.test.springboot.entity.Test;
import com.test.springboot.service.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {

    private final static Logger logger = LoggerFactory.getLogger(TestController.class);

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/query")
    public String test(@Valid Test test, BindingResult result) {
        if (result.hasErrors()) {
            return result.getFieldErrors().get(0).getDefaultMessage();
        }
        Member member = new Member();
        member.setRegNo(169090695);
        member.setNickName("2");
        member.setRealName("1");
        member = testService.appMember(member);
        return member.getRealName();
    }

    @RequestMapping("/update")
    public String update() {
        List<Member> members = new ArrayList<>();
        try {
            Member member2 = new Member();
            member2.setRegNo(221209924);
            member2.setNickName("张三1");
            member2.setRealName("张三丰");
            members.add(member2);
            int ret = testService.updateMember(members);
        } catch (Exception e) {
            logger.error("异常：", e);
        }
        return JSONArray.toJSONString(members);
    }

    @RequestMapping("/insert")
    public String insert() {
        Member member = new Member();
        member.setRegNo(169190695);
        member.setNickName("测试事物1");
        member.setRealName("牛慧霞1");
        int ret = testService.insertMember(member);
        return member.getRealName();
    }

}
