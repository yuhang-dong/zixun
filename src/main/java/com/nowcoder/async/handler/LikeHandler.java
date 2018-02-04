package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;

import java.util.Arrays;
import java.util.List;

public class LikeHandler implements EventHandler {


    @Override
    public void doHandler(EventModel model) {
        System.out.println("do like");
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}
