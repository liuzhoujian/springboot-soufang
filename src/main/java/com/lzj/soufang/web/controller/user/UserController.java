package com.lzj.soufang.web.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    //到达用户登录页面
    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login";
    }

    //到达用户中心
    @GetMapping("/user/center")
    public String centerPage() {
        return "user/center";
    }
}
