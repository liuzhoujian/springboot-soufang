package com.lzj.soufang.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于角色的登录入口控制器
 */
public class LoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint {

    //路径匹配工具
    private PathMatcher pathMatcher = new AntPathMatcher();

    //存储路径的map
    private final Map<String, String> authEntryPointMap;

    public LoginUrlEntryPoint(String loginFormUrl) {
        super(loginFormUrl);

        //初始化map
        authEntryPointMap = new HashMap<>();
        // 普通用户登录入口映射
        authEntryPointMap.put("/user/**", "/user/login");
        // 管理员登录入口映射
        authEntryPointMap.put("/admin/**", "/admin/login");
    }

    //http://localhost:8080/admin/center 登录 /login 当【管理员】登录错误时却跳转到【用户】登录出错页面？？BUG
    //http://localhost:8080/user/center  登录 /login 当【用户】登录出错时跳转到【用户】出错页面
    //个人觉得是父类是默认使用loginFormUrl,而loginFormUrl默认传递的是/user/login，因此默认用了这个

    //根据请求跳转到指定的页面，父类是默认使用loginFormUrl
    @Override
    protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        String uri = request.getRequestURI().replace(request.getContextPath(), "");

        for (Map.Entry<String, String> authEntry : this.authEntryPointMap.entrySet()) {
            if (this.pathMatcher.match(authEntry.getKey(), uri)) {
                return authEntry.getValue();
            }
        }

        return super.determineUrlToUseForThisRequest(request, response, exception);
    }
}
