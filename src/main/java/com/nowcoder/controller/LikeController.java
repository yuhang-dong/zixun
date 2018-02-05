package com.nowcoder.controller;


import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.News;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.NewsService;
import com.nowcoder.util.ToutiaoUtil;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private NewsService newsService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/like"})
    @ResponseBody
    public String like(@RequestParam("newsId") int newsId){
        int userId = hostHolder.get()!=null?hostHolder.get().getId():-1;
        if(userId == -1) {
            return ToutiaoUtil.getJSONString(1,"未登录");
        }
        long likeCount = likeService.like(userId, EntityType.ENTITYTYPE_NEWS,newsId);
        newsService.updateLikeCount(newsId,(int)likeCount);
        News news = newsService.getById(newsId);

        eventProducer.fireEvent(new EventModel(EventType.LIKE).setActorId(hostHolder.get().getId()).setEntityId(newsId)
        .setEntityType(EntityType.ENTITYTYPE_NEWS).setEntityOwnerId(news.getUserId()));
        return ToutiaoUtil.getJSONString(0,String.valueOf(likeCount));
    }


    @RequestMapping(path = {"/dislike"})
    @ResponseBody
    public String dislike(@RequestParam("newsId") int newsId){
        int userId = hostHolder.get().getId();
        long likeCount = likeService.disLike(userId, EntityType.ENTITYTYPE_NEWS,newsId);
        newsService.updateLikeCount(newsId,(int)likeCount);
        return ToutiaoUtil.getJSONString(0,String.valueOf(likeCount));
    }
}
