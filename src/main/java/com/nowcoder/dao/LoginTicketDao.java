package com.nowcoder.dao;

import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface LoginTicketDao {
    String TABLE_NAME = "login_ticket";
    String INSERT_FIELDS = " user_id, expired, status, ticket ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ",TABLE_NAME," ( ",INSERT_FIELDS," ) VALUES (#{userId},#{expired}, #{status},#{ticket} )"})
    int addTicket(LoginTicket ticket);

    @Select({"SELECT ", SELECT_FIELDS," FROM ",TABLE_NAME, " WHERE ticket = #{ticket}"})
    LoginTicket selectByTicket(String ticket);

    @Update({" update ", TABLE_NAME," SET status=#{status} WHERE ticket=#{ticket}"})
    void updateStatus(@Param("ticket") String ticket,@Param("status")int status);
}
