package com.nowcoder.community.controller;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * Developer：Foes
 */
@Controller
@RequestMapping("/letter")
public class MessageController {

    @Autowired
    private UserService userService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    //私信列表
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        //分页
        page.setPath("/letter/list");
        page.setLimit(5);
        User user = hostHolder.getUser();
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.findConversationByUserId(user.getId(),
                page.getLimit(), page.getOffset());
        List<Map<String,Object>> conversations =new ArrayList<>();
        if(conversationList!=null){
            for (Message conversation : conversationList){
                Map<String,Object> map =new HashMap<>();
                map.put("conversation",conversation);
                map.put("letterCount",messageService.findLettersCount(conversation.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId()
                        , conversation.getConversationId()));
                //判断当前会话的发起和传达是用户还是对方，因为本质上要让对方的头像显示
                int targetId =user.getId()==conversation.getFromId() ? conversation.getToId() : conversation.getFromId();
                map.put("target",userService
                        .findUserById(targetId));
                conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        return "/site/letter";
    }
    @RequestMapping(value = "/detail/{conversationId}",method = RequestMethod.GET)
    public String findLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        page.setLimit(5);
        page.setRows(messageService.findLettersCount(conversationId));
        page.setPath("/letter/detail/"+conversationId);
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> letters =new ArrayList<>();
        if(letterList!=null){
            for (Message message:letterList){
                Map<String,Object> map =new HashMap<>();
                map.put("message",message);
                map.put("fromUser",userService.findUserById(message.getFromId()));
                letters.add(map);
            }
            model.addAttribute("letters",letters);
        }
        model.addAttribute("target",findLetterTarget(conversationId));
        // 设置已读
        List<Integer> unreadIds = getUnreadIds(letterList);
        if(!unreadIds.isEmpty()){
            messageService.readMessage(unreadIds);
        }
        return "/site/letter-detail";
    }
    private User findLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        User user = hostHolder.getUser();
        if(id1==user.getId()){
            return userService.findUserById(id0);
        }else {
            return userService.findUserById(id1);
        }
    }
    private List<Integer> getUnreadIds(List<Message> messages){
        List<Integer> ids =new ArrayList<>();
        if(messages==null){
            throw  new IllegalArgumentException("参数不能为空");
        }
        for(Message message:messages){
            if(message.getToId()==hostHolder.getUser().getId()&&message.getStatus()==0){
                ids.add(message.getId());
            }
        }
        return ids;
    }
    @RequestMapping("/send")
    @ResponseBody
    public String sendMessage(String toName,String content){
        User fromUser = hostHolder.getUser();
        User toUser = userService.findUserByName(toName);
        if(toUser==null){
            return CommunityUtil.getJSONString(1,"目标用户不存在");
        }
        Message message = new Message();
        message.setContent(content);
        message.setToId(toUser.getId());
        message.setFromId(fromUser.getId());
        if(message.getFromId()>message.getToId()){
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }else{
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        }
       message.setStatus(0);
       message.setCreateTime(new Date());

       messageService.sendMessage(message);
        return CommunityUtil.getJSONString(0);
    }
}
