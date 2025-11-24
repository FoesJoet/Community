package com.nowcoder.community.controller;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
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
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"你还没有登录");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        //报错进行统一处理，此处只进行发布
        return CommunityUtil.getJSONString(0,"发布成功");
    }
    @RequestMapping("/detail/{discussPostId}")
    public String findDiscussPost(@PathVariable("discussPostId") int id, Model model, Page page){
        //帖子信息
        DiscussPost discussPost = discussPostService.findDiscussPostById(id);
        model.addAttribute("post",discussPost);
        //用户信息
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        //分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+id);
        page.setRows(discussPost.getCommentCount());

        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST,discussPost.getId(),page.getOffset(),page.getLimit());
        List<Map<String,Object>> commentVoList =new ArrayList<>();
        if(commentList!=null){
            for(Comment comment:commentList){
                Map<String,Object> commentVo =new HashMap<>();
                //评论内容
                commentVo.put("comment",comment);
                //作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //回复
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                if(replyList!=null) {
                    List<Map<String, Object>> replyVoList = new ArrayList<>();
                    for (Comment reply:replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复内容
                        replyVo.put("reply",reply);
                        //作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target=reply.getTargetId()!=0?userService.findUserById(reply.getTargetId()):null;
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }
                    //保存回复列表
                    commentVo.put("replys",replyVoList);
                    //回复数量
                    int replyCount = commentService.findCommentRowsByEntity(ENTITY_TYPE_COMMENT,comment.getId());
                    commentVo.put("replyCount",replyCount);

                    commentVoList.add(commentVo);
                }
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }
}
