package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Developer：Foes
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private MailClient mailClient;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int userId){
        return userMapper.selectById(userId);
    }

    public Map<String,Object> register(User user){
        //判断用户是否合法
        if(user==null){
            throw new IllegalArgumentException("用户不能为空");
        }
        Map<String ,Object> map =new HashMap<>();
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(userMapper.selectByName(user.getUsername())!=null){
            map.put("usernameMsg","该用户名已存在");
            return map;
        }
        if(userMapper.selectByEmail(user.getEmail())!=null){
            map.put("emailMsg","该邮箱已存在");
            return map;
        }

        //用户合法后创建并插入用户

        user.setCreateTime(new Date());
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt()));
        user.setStatus(0);
        user.setType(0);

        userMapper.insertUser(user);
        //发送邮件
        String url = domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        context.setVariable("url",url);
        String process = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"Activation",process);
        return map;
    }
    public int activation(int userid,String code){
        User user = userMapper.selectById(userid);
        if(user.getStatus()==1){
            return ACTIVATION_Repeated;
        }else if(code.equals(user.getActivationCode())){
            userMapper.updateStatus(userid,1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_Fail;
        }
    }
}
