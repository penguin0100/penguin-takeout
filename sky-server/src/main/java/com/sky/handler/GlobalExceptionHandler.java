package com.sky.handler;

import com.alibaba.druid.sql.ast.SQLAdhocTableSource;
import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    //捕获SQLIntegrityConstraintViolationException异常 SQL数据库错误
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error("异常信息：{}", ex.getMessage());
        //Duplicate entry 'zhangsan' for key 'employee idx_username'
        String message = ex.getMessage();//Duplicate entry 表示重复条目错误
        if(message.contains("Duplicate entry")){//比较错误信息包含Duplicate entry，说明是数据库中的字段重复了
            String[] split = message.split(" ");// split[2]是数据库中的字段名
            String name = split[2];
            String msg = name + MessageConstant.ALREADY_EXISTS;//拼接错误信息
            return Result.error(msg);
        }else{
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
