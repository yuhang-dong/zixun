package com.nowcoder.dao;

import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Repository
public class ImageDao {


    public List<String> getAllImage() {
        List<String> images = new ArrayList<String>();
        File folder = new File("E:\\tmp\\头像");
        for(File image : folder.listFiles()) {
            images.add(image.getName());
            //System.out.println(image.getName());
        }
        return images;
    }


    public static void main(String[] args) {
        ImageDao imageDao = new ImageDao();
        imageDao.getAllImage();
    }
}
