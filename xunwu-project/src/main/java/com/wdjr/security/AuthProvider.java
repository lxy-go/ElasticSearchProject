package com.wdjr.security;

import com.wdjr.entity.User;
import com.wdjr.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 自定义认证实现
 * 从数据库中获取用户名密码，获取user加载到内存去认证
 */

public class AuthProvider implements AuthenticationProvider {

    @Autowired
    private IUserService userService;

    private final Md5PasswordEncoder passwordEncoder =new Md5PasswordEncoder();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //获取用户名
        String userName = authentication.getName();
        //获取输入的密码
        String inputPassword = (String) authentication.getCredentials();
        User user = userService.findUserName(userName);
        if(user==null){
            throw  new AuthenticationCredentialsNotFoundException("authError");
        }

        if (this.passwordEncoder.isPasswordValid(user.getPassword(), inputPassword,user.getId() )){
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }
        throw new BadCredentialsException("authError");

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
