package com.test.springboot.interceptor;

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

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {
                MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class,
                ResultHandler.class})})
@Component
public class MyInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(MyInterceptor.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private Properties properties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        MappedStatement mappedStatement = (MappedStatement) invocation
                .getArgs()[0];
        String sqlId = mappedStatement.getId();
        Object parameter = invocation.getArgs()[1];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        String sql = getSql(configuration, boundSql, sqlId);
        sql = sql.replaceAll("`","");
        if (!StringUtils.isEmpty(sql)) {
            String tableName = null;
            Map<String, String> paramMap = new HashMap<String, String>();
            Map<String, String> conditionMap = new HashMap<String, String>();
            sql = sql.toLowerCase();
            if (SqlCommandType.UPDATE == sqlCommandType) {
                tableName = StringUtils.trimToEmpty(sql.substring(sql.indexOf("update ") + 7, sql.indexOf(" set")).trim());
                String values = sql.substring(sql.indexOf("set ") + 4, sql.indexOf(" where"));
                String[] params = values.split(",");

                for (String param : params) {
                    if (param.contains("=")) {
                        String[] ps = param.split("=");
                        paramMap.put(StringUtils.trimToEmpty(ps[0]), StringUtils.trimToEmpty(ps[1]));
                    }
                }
                String where = sql.substring(sql.indexOf("where ") + 6);
                String[] conditions = where.split(",");

                for (String condition : conditions) {
                    if (condition.contains("=")) {
                        String[] cs = condition.split("=");
                        conditionMap.put(StringUtils.trimToEmpty(cs[0]), StringUtils.trimToEmpty(cs[1]));
                    }
                }
            } else if (SqlCommandType.INSERT == sqlCommandType) {
                tableName = StringUtils.trimToEmpty(sql.substring(sql.indexOf("into ") + 5, sql.indexOf("(")));
                String params = StringUtils.trimToEmpty(sql.substring(sql.indexOf("("), sql.indexOf("values"))).replace("(", "").replace(")", "");
                String values = StringUtils.trimToEmpty(sql.substring(sql.indexOf("values") + 6)).replace("(", "").replace(")", "");
                String[] ps = params.split(",");
                String[] vs = values.split(",");
                if (ps.length == vs.length) {
                    for (int i = 0; i < ps.length; i++) {
                        paramMap.put(StringUtils.trimToEmpty(ps[i]), StringUtils.trimToEmpty(vs[i]));
                    }
                }
            } else if (SqlCommandType.DELETE == sqlCommandType) {
                tableName = StringUtils.trimToEmpty(sql.substring(sql.indexOf("from ") + 5, sql.indexOf("where")));
                String where = sql.substring(sql.indexOf("where ") + 6);
                String[] conditions = where.split(" and ");
                for (String condition : conditions) {
                    if (condition.contains("=")) {
                        String[] cs = condition.split("=");
                        conditionMap.put(StringUtils.trimToEmpty(cs[0]), StringUtils.trimToEmpty(cs[1]));
                    }
                }
            }
            logger.info("tableName:{}", tableName);
            logger.info("conditionMap:{}", conditionMap);
        }
        sendMQ(parameter);
        long endTime = System.currentTimeMillis();
        logger.info("当前程序耗时：" + (endTime - startTime) + "ms");
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private static String getSql(Configuration configuration, BoundSql boundSql, String sqlId) {
        try {
            String sql = showSql(configuration, boundSql);
            return sql;
        } catch (Exception e) {

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

    private void sendMQ(Object parameter) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        executorService.submit(() -> logger.info("parameter:{}", parameter));
                    }
                }
        );
    }
}
