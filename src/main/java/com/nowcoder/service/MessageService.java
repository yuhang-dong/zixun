package com.nowcoder.service;


import com.nowcoder.dao.MessageDao;
import com.nowcoder.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageDao messageDao;

    public int addMessage(Message message) {
        return messageDao.addMessage(message);
    }

    public List<Message> getConversationDetail(String conversationId,int offset,int limit) {
        return messageDao.getConversationDetail(conversationId,offset,limit);
    }

    public List<Message> getConversationList(int userId,int offset,int limit) {
        return messageDao.getConversationlist(userId,offset,limit);
    }

    public int getConversationCount(int userId,String conversationId) {
        return messageDao.getConversationCount(userId,conversationId);
    }

    public void readMessage(int messageId) {
        messageDao.readMessage(messageId);
    }

    public void deleteGroupMessage(String id,int userId) {
        messageDao.updateMessage(id,userId);
    }

    public void deleteMessage(int id) {
        messageDao.deleteMessage(id);
    }
}
