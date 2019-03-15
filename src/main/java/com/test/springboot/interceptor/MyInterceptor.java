package com.test.springboot.interceptor;

import com.alibaba.fastjson.JSONArray;
import com.test.springboot.util.MyRunnable;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {
                MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class})})
@Component
public class MyInterceptor implements Interceptor {

    private static final String[] cars = {"car", "trans_reply", "car_busy_time", "car_filter", "trans_filter",
            "car_tags", "member"};

    private static final Logger logger = LoggerFactory.getLogger(MyInterceptor.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation
                .getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        String sql = getSql(configuration, boundSql).replaceAll("`", "");
        if (!StringUtils.isEmpty(sql)) {
            String tableName = null;
            sql = sql.toLowerCase();
            if (SqlCommandType.UPDATE == sqlCommandType) {
                tableName = StringUtils.trimToEmpty(sql.substring(sql.indexOf("update ") + 7, sql.indexOf(" set")).trim());
            } else if (SqlCommandType.INSERT == sqlCommandType) {
                tableName = StringUtils.trimToEmpty(sql.substring(sql.indexOf("into ") + 5, sql.indexOf("(")));
            } else if (SqlCommandType.DELETE == sqlCommandType) {
                tableName = StringUtils.trimToEmpty(sql.substring(sql.indexOf("from ") + 5, sql.indexOf("where")));
            }
            if (SqlCommandType.UPDATE == sqlCommandType ||
                    SqlCommandType.INSERT == sqlCommandType ||
                    SqlCommandType.DELETE == sqlCommandType) {
                Object parameterObject = boundSql.getParameterObject();
                List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
                if (parameterMappings.size() > 0 && parameterObject != null) {
                    Object obj = null;
                    List<String> regNoList = new ArrayList<>();
                    for (ParameterMapping parameterMapping : parameterMappings) {
                        String propertyName = parameterMapping.getProperty();
                        if ("regNo".equals(propertyName) || propertyName.contains(".regNo")) {
                            MetaObject metaObject = configuration.newMetaObject(parameterObject);
                            if (metaObject.hasGetter(propertyName)) {
                                obj = metaObject.getValue(propertyName);
                            } else if (boundSql.hasAdditionalParameter(propertyName)) {
                                obj = boundSql.getAdditionalParameter(propertyName);
                            }
                            if (obj != null) {
                                regNoList.add(obj.toString());
                            }
                        }
                    }
                    if (!CollectionUtils.isEmpty(regNoList)) {
                        regNoList = regNoList.stream().distinct().collect(Collectors.toList());
                    }
                    logger.info("regNo={}", !CollectionUtils.isEmpty(regNoList) ? JSONArray.toJSONString(regNoList) : "");
                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private static String getSql(Configuration configuration, BoundSql boundSql) {
        try {
            return showSql(configuration, boundSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        return sql;
    }

    private static String getParameterValue(Object obj) {
        String value = "null";
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format((Date) obj) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            }
        }
        return value;
    }

    private void sendMQ(SqlCommandType sqlCommandType, String tableName, Map<String, String> paramMap, Map<String, String> conditionMap) {
        if (Arrays.asList(cars).contains(tableName)) {
            String regNo = "";
            if (sqlCommandType.equals(SqlCommandType.INSERT)) {
                regNo = paramMap.get("reg_no");
            } else if (sqlCommandType.equals(SqlCommandType.UPDATE) || sqlCommandType
                    .equals(SqlCommandType.DELETE)) {
                regNo = conditionMap.get("reg_no");
            }
            if (!StringUtils.isEmpty(regNo)) {
                String finalRegNo = regNo;
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronizationAdapter() {
                            @Override
                            public void afterCommit() {
                                MyRunnable runnable = new MyRunnable(finalRegNo);
                                executorService.submit(runnable);
                            }
                        }
                );
            }
        }
    }
}
