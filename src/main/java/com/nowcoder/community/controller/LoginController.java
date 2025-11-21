package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Developer：Foes
 */
@Controller
public class LoginController implements CommunityConstant {


    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Value("${server.servlet.context-path}")
    private String ContextPath;
    @Autowired
    private Producer kaptchaProducer;
    @Autowired
    private UserService userService;
    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }
    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,Object> map = userService.register(user);
        if(map==null||map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }
    @RequestMapping(value = "/activation/{userId}/{code}" ,method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId")int userId,@PathVariable("code") String code){
        int res = userService.activation(userId,code);
        if(res==ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，你可以开始使用帐号了");
            model.addAttribute("target", "/login");
        }else if(res== ACTIVATION_REPEATED){
            model.addAttribute("msg", "无效操作，用户已激活");
            model.addAttribute("target", "/index");
        }else {
            model.addAttribute("msg", "你提供的激活码不正确");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }
    @RequestMapping(value = "/kaptcha",method = RequestMethod.GET)
    public void getkaptcha(HttpServletResponse response, HttpSession session){
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        session.setAttribute("kaptcha",text);
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
           logger.error("响应验证码失败"+e.getMessage());
        }

    }
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(Model model,HttpSession session, HttpServletResponse response,
                        String username,String password, String code,
                        boolean rememberme){
        //验证验证码
        String kaptcha = (String)session.getAttribute("kaptcha");
        if (!kaptcha.equalsIgnoreCase(code)|| StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }
        //设置过期时间
        int expiredTime=0;
        if(rememberme){
            expiredTime=REMEMBER_EXPIRED_SECOND;
        }else{
            expiredTime=DEFAULT_EXPIRED_SECOND;
        }
        Map<String,Object> map =userService.login(username,password,expiredTime);
        if(map.containsKey("ticket")){
        String ticket = map.get("ticket").toString();
            Cookie cookie = new Cookie("ticket",ticket);
            cookie.setPath(ContextPath);
            cookie.setMaxAge(expiredTime);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }


    }
    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
            userService.logout(ticket);
            return "redirect:/login";
    }
}
