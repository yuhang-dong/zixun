package com.nowcoder.controller;


import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import com.sun.deploy.net.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @RequestMapping(path = {"/reg/"})
    @ResponseBody
    public String reg(Model model, @RequestParam("username") String username,
                      @RequestParam("password")String password,
                      @RequestParam(value = "remeber",defaultValue = "0") int remeber,
                      HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.register(username, password);
            if(!map.containsKey("ticket")) {
                return ToutiaoUtil.getJSONString(1,map);
            }
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath("/");
            if(remeber!=1) {
                cookie.setMaxAge(3600*24*1);
            } else {
                cookie.setMaxAge(3600*24*5);
            }

            response.addCookie(cookie);
            return ToutiaoUtil.getJSONString(0,"注册成功");
        }catch(Exception e) {
            logger.error("注册异常",e);
            return ToutiaoUtil.getJSONString(1,"注册异常");
        }
    }


    @RequestMapping(path = {"/login"})
    @ResponseBody
    public String login(Model model, @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value = "remeber",defaultValue = "0") int remeber,
                        HttpServletResponse response) {
        try {
            Map<String, Object> map = userService.login(username, password);
            if(!map.containsKey("ticket")) {
                return ToutiaoUtil.getJSONString(1,map);
            }
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath("/");
            if(remeber!=1) {
                cookie.setMaxAge(3600*1000*24*1);
            } else {
                cookie.setMaxAge(3600*1000*24*5);
            }
            response.addCookie(cookie);
            return ToutiaoUtil.getJSONString(0,"登录成功");
        }catch(Exception e) {
            logger.error("注册异常",e);
            return ToutiaoUtil.getJSONString(1,"注册异常");
        }

    }


    @RequestMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/";
    }
}
