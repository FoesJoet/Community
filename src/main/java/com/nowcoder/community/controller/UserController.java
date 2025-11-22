package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Developer：Foes
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private  String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String ContextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;
    @LoginRequired
    @RequestMapping(value = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }
    @LoginRequired
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String updateHeader(MultipartFile header, Model model){
        //文件验证
        if(header==null){
            model.addAttribute("error","您还未添加图片！");
            return "/site/setting";
        }
        String filename = header.getOriginalFilename();
        String suffix =filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确！");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID()+filename;
        File dest =new File(uploadPath+"/"+filename);
        try {
            header.transferTo(dest);
        } catch (IOException e) {
            logger.error("传输文件失败"+e.getMessage());
            throw  new RuntimeException("上传文件失败，服务器异常"+e.getMessage());
        }
        //更新当前用户的头像文件web访问路径
        //http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String filePath =domain+ContextPath+"/user"+"/header/"+filename;
        userService.updateHeader(user.getId(),filePath);
        return "redirect:/index";
    }

    @RequestMapping(value = "/header/{filename}",method = RequestMethod.GET)
    public void getHeaderImage(@PathVariable("filename") String filename, HttpServletResponse response){
        filename =uploadPath+"/"+filename;
        String suffix =filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/"+suffix);
        try( FileInputStream fileInputStream = new FileInputStream(filename);
             ServletOutputStream os = response.getOutputStream();) {
           byte[] bytes =new byte[1024];
           int b =0;
           while((b=fileInputStream.read(bytes))!=-1){
               os.write(bytes,0,b);
           }
        }catch (IOException e) {
            logger.error("读取头像失败"+e.getMessage());
        }
    }
    @LoginRequired
    @RequestMapping(value = "/updatepassword",method = RequestMethod.POST)
    public String updatePassword(Model model,String newPassword,String oldPassword,String confirmPassword){
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("originPasswordError","密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordError","密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(confirmPassword)){
            model.addAttribute("confirmPasswordError","密码不能为空");
            return "/site/setting";
        }
        if(!confirmPassword.equals(newPassword)){
            model.addAttribute("confirmPasswordError","两次密码不一致");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        if(!CommunityUtil.md5(oldPassword+user.getSalt()).equals(user.getPassword())){
            model.addAttribute("originPasswordError","原密码不正确");
            return "/site/setting";
        }
        userService.updatePassword(user.getId(),newPassword,user.getSalt());
        return "redirect:/index";
    }
}
