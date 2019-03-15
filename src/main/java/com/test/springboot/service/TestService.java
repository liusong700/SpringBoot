package com.test.springboot.service;

import com.test.springboot.entity.Member;
import com.test.springboot.mapper.TestMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    public Member appMember(Member member) {
        Map<String, Object> map = new HashMap<>();
        map.put("regNo", member.getRegNo());
        map.put("nickName", member.getNickName());
        map.put("realName", member.getRealName());
        return testMapper.queryMember2(member.getRegNo(), member.getNickName(), member.getRealName());
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateMember(List<Member> members) {
        int ret = testMapper.updateMember(members);
        List<String> list = new ArrayList<>();
        //logger.info(list.get(0));
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        System.out.println("事物提交后执行方法!");
                    }
                }
        );
        return ret;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertMember(Member member) {
        int ret = testMapper.insertMember(member);
        return ret;
    }


}
