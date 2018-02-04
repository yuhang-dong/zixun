package com.nowcoder.dao;

import com.nowcoder.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import javax.websocket.server.ServerEndpoint;
import java.util.List;

@Mapper
@Repository
public interface MessageDao {
    String TABLE_NAME = " message ";
    String INSERT_FIELDS = " from_id, to_id, content, has_read, conversation_id, created_date";
    String SELECT_FIELDS = " id, "+INSERT_FIELDS;


    @Insert({" INSERT INTO ",TABLE_NAME,"(",INSERT_FIELDS,")"," VALUES(#{fromId},#{toId},#{content},#{hasRead},#{conversationId},#{createdDate})"})
    int addMessage(Message message);


    @Select({" SELECT ",SELECT_FIELDS," FROM ",TABLE_NAME," WHERE conversation_id=#{conversationId} ORDER BY id DESC LIMIT #{offset},#{limit}"})
    List<Message> getConversationDetail(@Param("conversationId")String conversationId, @Param("offset")int offset,@Param("limit")int limit);

    // select * count(id) as id from (SELECT * FROM message where from_id=12 or to_id=12 order by id) tt group by conversation_id order by id

    @Select({" SELECT ",INSERT_FIELDS,", COUNT(id) as id FROM ","( SELECT * FROM ",TABLE_NAME ," WHERE from_id=#{userId} or to_id=#{userId} order by id desc) tt group by conversation_id order by id desc limit #{offset},#{limit}"})
    List<Message> getConversationlist(@Param("userId")int userId,
                                      @Param("offset")int offset,@Param("limit")int limit);


    @Select({"SELECT COUNT(id) FROM ",TABLE_NAME," WHERE has_read=0 and conversation_id=#{conversationId} and to_id=#{userId}"})
    int getConversationCount(@Param("userId") int userId,@Param("conversationId")String conversationId);

}
