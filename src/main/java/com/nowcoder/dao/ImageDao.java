package com.nowcoder.dao;

import com.nowcoder.model.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ImageDao {
    String TABLE_NAME = "image";
    String SELECT_FILED = " id, url ";

    @Select({ "SELECT " ,SELECT_FILED, " FROM " ,TABLE_NAME})
    List<Image> getAllImage();
}
