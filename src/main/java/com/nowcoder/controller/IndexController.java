package com.nowcoder.controller;

import com.nowcoder.dao.UserDao;
import com.nowcoder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Random;

// @Controller
public class IndexController {

    @Autowired
    UserDao userDao;
    @RequestMapping("/vm")
    public String test() {
        Random random = new Random();

        User user = new User();
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%d.png", random.nextInt()));
        user.setName("USER");
        user.setPassword("");
        user.setSalt("");
        userDao.addUser(user);
        return "new";
    }
}
