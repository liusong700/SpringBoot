package com.test.springboot.controller;

import com.test.springboot.entity.Member;
import com.test.springboot.service.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final static Logger logger = LoggerFactory.getLogger(TestController.class);

    private final TestService testService;

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @RequestMapping("/")
    public String test() {
        Member member = new Member();
        member.setRegNo(169090695);
        member.setNickName("2");
        member.setRealName("1");
        member = testService.appMember(member);
        return member.getRealName();
    }

    @RequestMapping("/update")
    public String update() {
        Member member = new Member();
        member.setRegNo(169090695);
        member.setNickName("测试事物");
        member.setRealName("牛慧霞");
        int ret = testService.updateMember(member);
        logger.info("ret:{}", ret);
        return member.getRealName();
    }

}
