package com.lzj.soufang.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/404")
    public String notFoundPage() {
        return "error/404";
    }

    @GetMapping("/403")
    public String accessError() {
        return "error/403";
    }

    @GetMapping("/500")
    public String internalError() {
        return "error/500";
    }

    @GetMapping("/logout/page")
    public String logoutPage() {
        return "logout";
    }
}