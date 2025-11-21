package com.nowcoder.community.utils;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * Developer：Foes
 * 用于在线程隔离的情况下持有用户信息
 * 使用ThreadLocal实现
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users= new ThreadLocal<User>();
    //存入用户凭证
    public void setUser(User user){
        users.set(user);
    }
    //取出用户信息
    public User getUser(){
        return users.get();
    }
    //删除用户信息
    public void clear(){
        users.remove();
    }
}
