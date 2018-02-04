package com.nowcoder.async;

import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

public class EventConsumer implements InitializingBean,ApplicationContextAware{

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String,EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if(beans!=null) {
            for(Map.Entry<String,EventHandler> entry: beans.entrySet()) {
                List
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
