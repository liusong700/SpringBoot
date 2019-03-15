package com.test.springboot.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author song.liu
 */
@Aspect
@Component
@Slf4j
public class MapperAspect {


    @Pointcut("execution(* com.test.springboot.mapper.*Mapper.*(..))")
    private void pointCutMethod() {
    }

    @Around("pointCutMethod()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        long begin = System.currentTimeMillis();
        Object obj = pjp.proceed();
        long end = System.currentTimeMillis();
        log.info("调用Mapper方法：{}，参数：{}，耗时：{}毫秒",
                pjp.getSignature().toString(), Arrays.toString(pjp.getArgs()), (end - begin));
        return obj;
    }

}
