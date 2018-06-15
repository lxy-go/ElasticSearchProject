package com.wdjr.base;

import com.wdjr.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户
 */
public class LoginUserUtil {

    public static User load(){
        //获取认证用户
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal != null && principal instanceof User){
            return (User) principal;
        }
        return null;
    }

    public static Long getLoginUserId(){
        User user = load();
        if (user ==null){
            return -1L;
        }
        return user.getId();
    }
}
