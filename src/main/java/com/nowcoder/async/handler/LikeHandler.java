package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class LikeHandler implements EventHandler {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;
    @Override
    public void doHandler(EventModel model) {
        // 给被点赞的人发消息
        Message message = new Message();
        message.setFromId(model.getActorId());
        User user = userService.getUser(model.getActorId());
        message.setToId(model.getEntityOwnerId());
        message.setConversationId(message.getFromId()<message.getToId()?String.format("%d_%d",message.getFromId(),message.getToId()):String.format("%d_%d",message.getToId(),message.getFromId()));
        message.setContent("用户" + user.getName() + "赞了你的资讯,http://127.0.0.1:8080/news/" + model.getEntityId());
        message.setCreatedDate(new Date());
        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}
