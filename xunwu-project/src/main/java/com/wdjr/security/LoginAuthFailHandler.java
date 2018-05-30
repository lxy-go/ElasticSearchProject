package com.wdjr.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 验证失败处理器
 */

public class LoginAuthFailHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginUrlEntryPoint urlEntryPoint;

    public LoginAuthFailHandler(LoginUrlEntryPoint urlEntryPoint) {
        this.urlEntryPoint = urlEntryPoint;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
    //1.获取跳转的url
        String tagetUrl = this.urlEntryPoint.determineUrlToUseForThisRequest(request, response, exception);
        tagetUrl +="?"+exception.getMessage();
        super.setDefaultFailureUrl(tagetUrl);
        super.onAuthenticationFailure(request, response, exception);
    }
}
