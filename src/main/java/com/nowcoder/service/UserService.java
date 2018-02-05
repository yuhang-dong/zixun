package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDao;
import com.nowcoder.dao.UserDao;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.HeadUtil;
import com.nowcoder.util.ToutiaoUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private HeadUtil headUtil;

    @Autowired
    private LoginTicketDao loginTicketDao;

    public User getUser(int id) {
        return userDao.selectById(id);
    }

    public Map<String,Object> register(String username, String password) {
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)) {
            map.put("msgname","用户名不能为空");
        }

        if(StringUtils.isBlank(username)) {
            map.put("msgpwd","密码不能为空");
        }

        User user = userDao.selectByName(username);
        if(user!=null) {
            map.put("msgname","用户名已被注册");
        }

        if(!map.isEmpty()) {
            return map;
        }
        user = new User();
        user.setName(username);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setHeadUrl(headUtil.getRandomUrl());
        user.setPassword(ToutiaoUtil.MD5(password+user.getSalt()));
        userDao.addUser(user);
        String ticket = addTicket(user.getId());
        map.put("ticket",ticket);
        return map;
    }


    public Map<String,Object> login(String username, String password) {
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)) {
            map.put("msgname","用户名不能为空");
        }

        if(StringUtils.isBlank(username)) {
            map.put("msgpwd","密码不能为空");
        }

        User user = userDao.selectByName(username);
        if(user==null) {
            map.put("msgname","用户名不存在");
            return map;
        }

        if(!ToutiaoUtil.MD5(password+user.getSalt()).equals(user.getPassword())) {
            map.put("msgpwd","密码错误");
        }

        if(!map.isEmpty()) {
            return map;
        }
        String ticket = addTicket(user.getId());
        map.put("ticket",ticket);
        map.put("userId",user.getId());
        return map;
    }

    private String addTicket(int userId) {
        LoginTicket loginTicket = new LoginTicket();
        Date date = new Date();
        date.setTime(date.getTime()+1000*3600*24);
        loginTicket.setExpired(date);
        loginTicket.setTicket(UUID.randomUUID().toString().replaceAll("-",""));
        loginTicket.setUserId(userId);
        loginTicketDao.addTicket(loginTicket);
        return loginTicket.getTicket();
    }

    public void logout(String ticket) {
        loginTicketDao.updateStatus(ticket,1);
    }
}
