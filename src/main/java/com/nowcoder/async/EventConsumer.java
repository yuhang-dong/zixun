package com.nowcoder.async;

import com.alibaba.fastjson.JSON;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.xml.ws.ServiceMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventConsumer implements InitializingBean,ApplicationContextAware{

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);
    private ApplicationContext applicationContext;
    private Map<EventType,List<EventHandler>> config = new HashMap<EventType,List<EventHandler>>();

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String,EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if(beans!=null) {
            for(Map.Entry<String, EventHandler> bean : beans.entrySet()) {
                List<EventType> eventTypes = bean.getValue().getSupportEventTypes();
                for(EventType type : eventTypes) {
                    if(!config.containsKey(type)) {
                        config.put(type,new ArrayList<EventHandler>());
                    }
                    config.get(type).add(bean.getValue());
                    System.out.println(type.toString() + ":" + bean.getValue().toString());
                }
            }
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    String key = RedisKeyUtil.getEventQueueKey();
                    List<String> events = jedisAdapter.brpop(0,key);
                    for(String msg : events) {
                        if(msg.equals(key)) {
                            continue;
                        }
                        EventModel eventModel = JSON.parseObject(msg,EventModel.class);
                        if(!config.containsKey(eventModel.getType())) {
                            LOGGER.error("不能识别的事件");
                            continue;
                        }
                        for(EventHandler handler : config.get(eventModel.getType())) {
                            handler.doHandler(eventModel);
                        }
                    }
                    //System.out.println("???");
                }
            }
        });
        thread.start();
    }
}
