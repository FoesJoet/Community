package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Developer：Foes
 */
@Mapper
public interface MessageMapper {
    //查询当前用户的总会话列表,并返回最新的一条私信
    List<Message> selectConversation(int userId,int offset,int limit);
    //查询当前用户的会话数量
    int selectConversationCount(int userId);
    //查询会话所包含的私信列表
    List<Message> selectLetters(String conversationId,int offset,int limit);
    //查询会话包含的私信数量
    int selectLettersCount(String conversationId);
    //查询会话未读消息
    int selectUnreadCount(int userId,String conversationId);

    int insertMessage(Message message);

    int updateMessageStatus(List<Integer> ids,int status);
}
