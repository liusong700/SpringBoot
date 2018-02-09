package com.test.springboot.service;

import com.test.springboot.entity.Member;
import com.test.springboot.mapper.TestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestService {

    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    private final TestMapper testMapper;

    @Autowired
    public TestService(TestMapper testMapper) {
        this.testMapper = testMapper;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Member appMember(Member member) {
        Map<String, Object> map = new HashMap<>();
        map.put("regNo", member.getRegNo());
        map.put("nickName", member.getNickName());
        map.put("realName", member.getRealName());
        //return testMapper.appMember(map);
        //return testMapper.queryMember1(member.getRegNo());
        return testMapper.queryMember2(member.getRegNo(), member.getNickName(), member.getRealName());
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateMember(Member member){
        int ret = testMapper.updateMember(member);
        List<String> list = new ArrayList<>();
        //logger.info(list.get(0));
        return ret;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertMember(Member member){
        int ret = testMapper.insertMember(member);
        List<String> list = new ArrayList<>();
        logger.info(list.get(0));
        return ret;
    }


}
