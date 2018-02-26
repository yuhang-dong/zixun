package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class READMESSAGEHandler implements EventHandler {

    @Autowired
    MessageService messageService;

    @Override
    public void doHandler(EventModel model) {
        int messageId = model.getEntityOwnerId();
        messageService.readMessage(messageId);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.READMESSAGE);
    }
}
