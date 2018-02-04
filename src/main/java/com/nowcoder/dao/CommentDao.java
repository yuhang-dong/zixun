package com.nowcoder.dao;


import com.nowcoder.model.Comment;
import com.nowcoder.model.News;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CommentDao {

    String TABLE_NAME = " comment ";
    String INSERT_FIELDS = " user_id, content, created_date, entity_id, entity_type, status ";
    String SELECT_FIELDS = " id, "+INSERT_FIELDS;

    @Insert({" INSERT INTO ",TABLE_NAME,"(",INSERT_FIELDS,") VALUES (#{userId},#{content},#{createdDate},#{entityId},#{entityType},#{status})"})
    int addComment(Comment comment);


    @Select({" SELECT ",SELECT_FIELDS," FROM ",TABLE_NAME," WHERE entity_id=#{entityId} and entity_type=#{entityType} ORDER BY id DESC"})
    List<Comment> selectByEntity(@Param("entityId")int entityId,@Param("entityType")int entityType);


    @Select({" SELECT COUNT(id) FROM ",TABLE_NAME," WHERE entity_id=#{entityId} and entity_type=#{entityType}"})
    int getCommentCout(@Param("entityId")int entityId,@Param("entityType")int entityType);

}
