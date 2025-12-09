package com.sky.context;

public class BaseContext {

    //当前线程的id
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    //设置当前员工id
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }
    //获取当前员工id
    public static Long getCurrentId() {
        return threadLocal.get();
    }
    //删除当前员工id
    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
