package com.nowcoder.community;

import com.nowcoder.community.utils.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Developer：Foes
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
   private TemplateEngine templateEngine;
    @Autowired
    private MailClient mailClient;
    @Test
    public void sendMail(){
        mailClient.sendMail("huangrh04@163.com","Test","Hello");
    }
    @Test
    public void sendHtmlMail(){
        Context context =new Context();
        context.setVariable("username","666");
        String con = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("huangrh04@163.com","Test",con);
    }

}
