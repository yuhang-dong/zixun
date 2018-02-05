package com.nowcoder;

import com.nowcoder.dao.*;
import com.nowcoder.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ToutiaoApplication.class)
@Sql("/init-schema.sql")
public class InitDatabaseTests {

    @Autowired
    UserDao userDao;

    @Autowired
    NewsDao newsDao;

    @Autowired
    ImageDao imageDao;

    @Autowired
    LoginTicketDao loginTicketDao;

    @Autowired
    CommentDao commentDao;

    @Test
    public void contextLoads() {
        Random random = new Random();
        List<String> images = imageDao.getAllImage();
        String image = "http://127.0.0.1:8080/image?name=";
        int imagesSize = images.size();
        for(int i=0;i<11;i++) {
            User user = new User();
            user.setHeadUrl(image+images.get((int)(Math.random()*imagesSize)));
            user.setName("USER"+i);
            user.setPassword("");
            user.setSalt("");
            userDao.addUser(user);

            News news = new News();
            news.setCommentCount(i);
            Date date = new Date();
            date.setTime(date.getTime()-3600000*10*i);
            System.out.println(date);
            news.setCreatedDate(date);
            news.setImage(image+images.get((int)(Math.random()*imagesSize)));
            news.setLikeCount(i);
            news.setLink(String.format("https://www.baidu.com/%d",random.nextInt()));
            news.setTitle("TITLE"+String.valueOf(i));
            news.setUserId(i+1);
            newsDao.addNews(news);

            for(int j=0;j<5;j++) {
                Comment comment = new Comment();
                comment.setUserId(user.getId());
                comment.setCreatedDate(new Date());
                comment.setEntityId(news.getId());
                comment.setEntityType(EntityType.ENTITYTYPE_NEWS);
                comment.setStatus(0);
                comment.setContent("COMMNET TO "+comment.getEntityId());
                commentDao.addComment(comment);
            }

            user.setPassword("newPass");
            userDao.updatePassword(user);

            LoginTicket loginTicket = new LoginTicket();
            loginTicket.setUserId(i+1);
            loginTicket.setExpired(date);
            loginTicket.setStatus(0);
            loginTicket.setTicket("USER"+i);
            loginTicketDao.addTicket(loginTicket);
            loginTicketDao.updateStatus("USER"+i,2);
        }
        Assert.assertEquals("newPass",userDao.selectById(1).getPassword());
        Assert.assertEquals(2,loginTicketDao.selectByTicket("USER0").getStatus());
    }

    @Test
    public void testImage() {
        List<String> images = imageDao.getAllImage();
        Assert.assertNotNull(images);
    }
}
