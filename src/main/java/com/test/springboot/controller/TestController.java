package com.test.springboot.controller;

import com.test.springboot.entity.Member;
import com.test.springboot.service.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class TestController {

    private final static Logger logger = LoggerFactory.getLogger(TestController.class);

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/")
    public String index(){
        return "index";
    }

    @RequestMapping("/query")
    @ResponseBody
    public String test() {
        Member member = new Member();
        member.setRegNo(169090695);
        member.setNickName("2");
        member.setRealName("1");
        member = testService.appMember(member);
        return member.getRealName();
    }

    @RequestMapping("/update")
    @ResponseBody
    public String update() {
        Member member = new Member();
        member.setRegNo(169090695);
        member.setNickName("测试事物");
        member.setRealName("牛慧霞");
        int ret = testService.updateMember(member);
        return member.getRealName();
    }

    @RequestMapping("/insert")
    @ResponseBody
    public String insert() {
        Member member = new Member();
        member.setRegNo(169190695);
        member.setNickName("测试事物1");
        member.setRealName("牛慧霞1");
        int ret = testService.insertMember(member);
        return member.getRealName();
    }

}
