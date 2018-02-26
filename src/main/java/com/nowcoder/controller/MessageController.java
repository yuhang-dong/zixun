package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.dao.MessageDao;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.model.ViewObject;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.swing.text.View;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MessageController {
    private final static Logger LOGGER = LoggerFactory.getLogger(MessageController.class);


    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;


    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = {"/msg/addMessage"},method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("fromId")int fromId,
                             @RequestParam("toId")int toId,
                             @RequestParam("content") String content){
        try {
            Message message = new Message();
            message.setContent(content);
            message.setCreatedDate(new Date());
            message.setFromId(fromId);
            message.setToId(toId);
            message.setHasRead(0);
            message.setConversationId(fromId<toId?String.format("%d_%d",fromId,toId):String.format("%d_%d",toId,fromId));
            messageService.addMessage(message);
            return ToutiaoUtil.getJSONString(0,String.valueOf(message.getId()));
        } catch (Exception e) {
            LOGGER.error("评论失败",e);
            return ToutiaoUtil.getJSONString(1,"评论失败");
        }
    }

    @RequestMapping(path = {"/msg/detail"},method = {RequestMethod.GET})
    public String conversationDetail(Model model, @Param("conversationId")String conversationId) {
        try {
            List<Message> conversationDetails = messageService.getConversationDetail(conversationId, 0, 10);
            List<ViewObject> messages = new ArrayList<ViewObject>();
            for (Message conversationDetail : conversationDetails) {
                ViewObject vo = new ViewObject();
                vo.set("message", conversationDetail);
                User user = userService.getUser(conversationDetail.getFromId());
                if (user == null) {
                    continue;
                }
                vo.set("headUrl", user.getHeadUrl());
                vo.set("userId", user.getId());
                messages.add(vo);
                if(conversationDetail.getHasRead() == 0) {
                    // 设置为读
                    eventProducer.fireEvent(new EventModel(EventType.READMESSAGE).setEntityOwnerId(conversationDetail.getId()));
                }
            }
            model.addAttribute("messages", messages);
            model.addAttribute("conversationId", conversationId);
        }catch(Exception e){
            LOGGER.error("详情页错误",e);
        }
        return "letterDetail";
    }

    @RequestMapping(path = {"/msg/list"})
    public String conversationList(Model model) {
        try {
            int localUserId = hostHolder.get().getId();
            List<ViewObject> conversations = new ArrayList<ViewObject>();
            List<Message> conversationList = messageService.getConversationList(localUserId,0,10);
            for(Message message : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("conversation",message);
                int targetId = message.getFromId()==localUserId?message.getToId():message.getFromId();
                User user = userService.getUser(targetId);
                vo.set("target",user);
                vo.set("unread",messageService.getConversationCount(localUserId,message.getConversationId()));
                vo.set("user",user);
                conversations.add(vo);
            }
            model.addAttribute("conversations",conversations);
        } catch (Exception e) {
            LOGGER.error("站内信列表失败",e);

        }
        return "letter";
    }

    @RequestMapping("/msg/deleteAllGroup?id=")
    public String deleteGroupMessage(@RequestParam("id")String id) {
        // delete current user all message
        int localUserId = hostHolder.get().getId();
        messageService.deleteGroupMessage(id,localUserId);
        return "redirect:/msg/list";
    }


    @RequestMapping("/msg/deleteMessage")
    public String deleteMessage(@RequestParam("id")int id,@RequestParam("conversationId")String conversationId) {
        int localUserId = hostHolder.get().getId();
        // 确保该用户对该信息有修改权限
        if(conversationId.contains(String.valueOf(localUserId))) {
            messageService.deleteMessage(id);
        }
        return "redirect:/msg/detail?conversationId="+conversationId;
    }
}
