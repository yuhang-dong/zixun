package com.nowcoder.util;

import com.nowcoder.dao.ImageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
public class HeadUtil {

    @Autowired
    ImageDao imageDao;


    public String getRandomUrl() {
        List<String> images = imageDao.getAllImage();
        return ToutiaoUtil.DOMAIN_URL+"image?name="+images.get((int)(Math.random()*images.size()));
    }
}
