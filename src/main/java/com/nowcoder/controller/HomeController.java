package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.NewsService;
import com.nowcoder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    NewsService newsService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    LikeService likeService;

    @RequestMapping(path = {"/","/index"})
    public String index(Model model, @RequestParam(value = "pop",defaultValue = "0")int pop) {

        model.addAttribute("vos",getNews(0,0,10));
        model.addAttribute("pop",pop);
        return "home";
    }

    @RequestMapping(path = {"/user/{userId}"})
    public String userIndex(Model model,@PathVariable("userId")int userId) {
        model.addAttribute("vos",getNews(userId,0,10));
        return "home";
    }

    @RequestMapping(path = {"/setting"})
    @ResponseBody
    public String settingIndex(Model model,@PathVariable("userId")int userId) {

        return "setting";
    }


    private List<ViewObject> getNews(int userId,int offset,int limit) {
        List<News> newsList = newsService.getLatestNews(userId,offset,limit);
        List<ViewObject> vos = new ArrayList<>(newsList.size());
        int localUserId = hostHolder.get()!=null?hostHolder.get().getId():0;
        for(News news: newsList) {
            ViewObject vo = new ViewObject();
            vo.set("news",news);
            vo.set("user",userService.getUser(news.getUserId()));

            if(localUserId!=0) {
                vo.set("like",likeService.getLikeStatus(localUserId, EntityType.ENTITYTYPE_NEWS,news.getId()));
            } else {
                vo.set("like",0);
            }
            vos.add(vo);


        }
        return vos;
    }
}
