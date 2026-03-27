package com.sky.aspect;

import com.sky.annotation.AutoFill;

import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    // 切点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    // AOP切点(Pointcut)方法
    public void autoFillPointCut(){// 将公共的切点表达式抽象出来，方便其他地方引用
    }
    // 前置通知：为公共字段赋值
    @Before("autoFillPointCut()")// 前置通知引用切点方法:autoFillPointCut()
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充");
        // 获取当前被拦截的方法参数，即实体对象  (反射)
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();// 获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);// 获取填充方式
        OperationType operationType = autoFill.value();// 获取操作类型
        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];
        // 获取当前操作类型 准备赋值对象数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        // 根据对应的操作类型，为对应的属性赋值
        if (operationType == OperationType.INSERT) {
            //为公共字段赋值
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod( "setCreateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod( "setCreateUser", Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod( "setUpdateUser", Long.class);
                // 为对应的属性赋值 (反射 暴力赋值)
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod( "setUpdateUser", Long.class);
                // 为对应的属性赋值 (反射 暴力赋值)
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
