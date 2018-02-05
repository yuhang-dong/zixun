package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Message;
import com.nowcoder.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.ws.handler.Handler;
import java.util.Arrays;
import java.util.List;

@Component
public class LoginExceptionHandler implements EventHandler{
    @Autowired
    MessageService messageService;
    @Override
    public void doHandler(EventModel model) {
        // TODO  登陆异常返回
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);
    }
}
