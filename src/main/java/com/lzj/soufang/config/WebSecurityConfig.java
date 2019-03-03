package com.lzj.soufang.config;

import com.lzj.soufang.security.LoginAuthFailHandler;
import com.lzj.soufang.security.LoginUrlEntryPoint;
import com.lzj.soufang.security.user.AuthProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * HTTP权限控制
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //定制请求的授权规则
        http.authorizeRequests()
                .antMatchers("/admin/login").permitAll() //管理员登录入口
                .antMatchers("/user/login").permitAll() //用户登录入口
                .antMatchers("/static/**").permitAll()  //静态资源
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN","USER") //有两个之一即可
                .antMatchers("/api/user/**").hasAnyRole("ADMIN", "USER");

        //开启自动配置的登录功能
        http.formLogin()
            //.loginPage("/admin/login") //跳转到自己定制的登录页面【使用另一种方式LoginUrlAuthenticationEntryPoint，统一配置用户和管理员的登录地址】
            .loginProcessingUrl("/login") // 配置角色登录处理入口（默认）
            .failureHandler(authFailHandler());//登录错误的处理机制

        //定制自己的登录页面,使用该方法后，登录错误要自己处理
        http.exceptionHandling()
                .authenticationEntryPoint(urlEntryPoint())
                .accessDeniedPage("/403");

        //开启退出功能，退出到登陆页面,删除cookie
        http.logout()
            .logoutUrl("/logout") //处理登出的路径（默认）
            .logoutSuccessUrl("/logout/page") //登出后到达的页面
            .deleteCookies("JSESSIONID")
            .invalidateHttpSession(true); //使httpsession无效

        //开启记住我功能
        http.rememberMe();

        http.csrf().disable();
        //in a frame because it set 'X-Frame-Options' to 'DENY'解决方法，配置这句话
        http.headers().frameOptions().sameOrigin();
    }

    @Bean
    public LoginAuthFailHandler authFailHandler() {
        return new LoginAuthFailHandler(urlEntryPoint());
    }

    //将用户和管理员地址，没有登录时映射到对应的登录页面
    @Bean
    public LoginUrlEntryPoint urlEntryPoint() {
        return new LoginUrlEntryPoint("/user/login");
    }

    @Bean
    public AuthProvider authProvider() {
        return new AuthProvider();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //在数据库中定制角色规则
        auth.authenticationProvider(authProvider()).eraseCredentials(true);
    }

    /* @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //在内存中定制角色规则
        auth.inMemoryAuthentication().
                withUser("admin").
                password("admin").
                roles("ADMIN")
                .and()
                .passwordEncoder(new CustomPasswordEncoder());
    }*/
}
