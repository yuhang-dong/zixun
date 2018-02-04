package com.nowcoder.service;

import com.nowcoder.dao.CommentDao;
import com.nowcoder.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentDao commentDao;


    public List<Comment> getCommentByEntity(int entityId,int entityType) {
        return commentDao.selectByEntity(entityId,entityType);
    }

    public int addCommnet(Comment comment) {
        return commentDao.addComment(comment);
    }

    public int getCommentCount(int entityId,int entityType) {
        return commentDao.getCommentCout(entityId,entityType);
    }
}
