package com.nowcoder.dao;


import com.nowcoder.model.News;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface NewsDao {
    String TABLE_NAME = "news";
    String INSERT_FIELDS = " title, link, image, like_count, comment_count, created_date, user_id ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({" insert into " + TABLE_NAME + " ( "+INSERT_FIELDS+" ) " +
     " VALUES(#{title},#{link},#{image},#{likeCount},#{commentCount}, #{createdDate}, #{userId} )"})
    int addNews(News news);

    List<News> selectByUserIdAndOffset(@Param("userId") int userId,@Param("offset") int offset
                                        ,@Param("limit") int limit);

    @Select({" SELECT ",SELECT_FIELDS," FROM ",TABLE_NAME," WHERE id=#{newsId}"})
    News getById(int newsId);


    @Update({" UPDATE ",TABLE_NAME," SET comment_count=#{count} WHERE id=#{newsId}"})
    void updateCommentCount(@Param("newsId") int newsId,@Param("count") int count);

    @Update({" UPDATE ",TABLE_NAME," SET like_count=#{count} WHERE id=#{newsId}"})
    void updateLikeCount(@Param("newsId") int newsId,@Param("count") int count);
}
