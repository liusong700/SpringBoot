package com.test.springboot.mapper;

import com.test.springboot.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface TestMapper {

    Member appMember(Map<String,Object> member);

    Member queryMember1(Integer regNo);

    Member queryMember2(@Param("regNo") Integer regNo,@Param("nickName") String nickName,@Param("realName") String realName);

    int updateMember(Member member);

    int insertMember(Member member);
}
