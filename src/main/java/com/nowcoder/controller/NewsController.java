package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.LikeService;
import com.nowcoder.service.NewsService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.swing.text.View;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class NewsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsController.class);


    @Autowired
    private UserService userService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    LikeService likeService;

    @RequestMapping(path = {"/uploadImage/"},method = {RequestMethod.POST})
    @ResponseBody
    public String uploadImage(@RequestParam("file")MultipartFile file) {
        try {
            String fileUrl = newsService.saveImage(file);
            if(fileUrl == null) {
                return ToutiaoUtil.getJSONString(1);
            }
            return ToutiaoUtil.getJSONString(0,fileUrl);
        } catch(Exception e) {
            LOGGER.error("上传图片失败",e);
            return ToutiaoUtil.getJSONString(1,"上传失败");
        }
    }

    @RequestMapping(path={"/image"},method = {RequestMethod.GET})
    @ResponseBody
    public void getImage(@RequestParam("name")String fileName, HttpServletResponse response) {
        response.setContentType("image/jpeg");
        try {
            StreamUtils.copy(new FileInputStream(new File(ToutiaoUtil.IMAGE_PATH + fileName)), response.getOutputStream());
        }catch(Exception e) {
            LOGGER.error("请求图片失败",e);
        }

    }


    @RequestMapping(path = {"/user/addNews"},method = {RequestMethod.POST})
    @ResponseBody
    public String addNews(@RequestParam("image") String image,
                          @RequestParam("title") String title,
                          @RequestParam("link") String link) {
        try {
            News news = new News();
            User user = hostHolder.get();
            if(user!=null) {
                news.setUserId(user.getId());
            } else {
                // 设置为匿名用户
                news.setUserId(-1);
            }
            news.setTitle(title);
            news.setLink(link);
            news.setCreatedDate(new Date());
            news.setLikeCount(0);
            news.setImage(image);
            System.out.println(title);
            newsService.addNews(news);
            return ToutiaoUtil.getJSONString(0);
        }catch(Exception e) {
            LOGGER.error("添加资讯错误",e);
            return ToutiaoUtil.getJSONString(1,"发布失败");
        }
    }

    @RequestMapping(path = {"/news/{newsId}"},method = {RequestMethod.GET})
    public String newDetail(@PathVariable("newsId") int newsId, Model model) {
        News news = newsService.getById(newsId);
        User owner = userService.getUser(news.getUserId());
        int localUserId = hostHolder.get()!=null?hostHolder.get().getId():0;
        if(news!=null) {
            List<Comment> commentList = commentService.getCommentByEntity(news.getId(), EntityType.ENTITYTYPE_NEWS);
            List<ViewObject> commentVOs = new ArrayList<ViewObject>();
            for(Comment comment : commentList) {
                ViewObject vo = new ViewObject();
                vo.set("comment",comment);
                vo.set("user",userService.getUser(comment.getUserId()));
                commentVOs.add(vo);
            }
            model.addAttribute("comments",commentVOs);
        }
        if(localUserId!=0) {
            model.addAttribute("like", likeService.getLikeStatus(localUserId,EntityType.ENTITYTYPE_NEWS,newsId));
        } else {
            model.addAttribute("like", 0);
        }
        model.addAttribute("news",news);
        model.addAttribute("owner",owner);
        return "detail";
    }

    @RequestMapping(path = {"/addComment"},method = {RequestMethod.POST})
    public String addCommnet(@RequestParam("newsId") int newsId,
                             @RequestParam("content")String content) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setUserId(hostHolder.get().getId());
            comment.setEntityType(EntityType.ENTITYTYPE_NEWS);
            comment.setEntityId(newsId);
            comment.setCreatedDate(new Date());
            comment.setStatus(0);
            commentService.addCommnet(comment);
            // 可以作为异步
            int count = commentService.getCommentCount(comment.getEntityId(),comment.getEntityType());
            newsService.updateCommentCount(newsId,count);
        }catch(Exception e) {
            LOGGER.error("添加评论出错",e);
        }
        return "redirect:/news/"+newsId;
    }
}
