package com.zhou;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.collections.map.HashedMap;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by 10124 on 2017/6/23.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ApplicationTests {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VelocityEngine velocityEngine;

    /**
     * @throws Exception
     * @description: 简单邮件
     */
    @Test
    public void sendSimpleMail() throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("zhouzhen@chinadaas.com");
        message.setTo("1012476676@qq.com");
        message.setSubject("主题：简单邮件");
        message.setText("测试邮件内容");
        mailSender.send(message);
    }

    /**
     * @throws Exception
     * @description: 带附件邮件
     */
    @Test
    public void sendAttachmentsMail() throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom("zhouzhen@chinadaas.com");
        helper.setTo("1012476676@qq.com");
        helper.setSubject("主题：有附件");
        helper.setText("有附件的邮件");
        FileSystemResource file = new FileSystemResource(new File("weixin.jpg"));
        helper.addAttachment("附件-1.jpg", file);
        helper.addAttachment("附件-2.jpg", file);
        mailSender.send(mimeMessage);
    }

    /**
     * @throws Exception
     * @description: 插入图片邮件
     */
    @Test
    public void sendInlineMail() throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom("zhouzhen@chinadaas.com");
        helper.setTo("1012476676@qq.com");
        helper.setSubject("主题：嵌入静态资源");
        helper.setText("<html><body><img src=\"cid:weixin\" ></body></html>", true);
        FileSystemResource file = new FileSystemResource(new File("weixin.jpg"));
        helper.addInline("weixin", file);
        mailSender.send(mimeMessage);
    }

    /**
     * @throws Exception
     * @description: 带velocity模块的模板邮件。
     */
    @Test
    public void sendTemplateMail() throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom("zhouzhen@chinadaas.com");
        helper.setTo("1012476676@qq.com");
        helper.setSubject("主题：模板邮件");
        Map<String, Object> model = new HashedMap();
        model.put("username", "didi");
        String text = VelocityEngineUtils.mergeTemplateIntoString(
                velocityEngine, "template.vm", "UTF-8", model);
        helper.setText(text, true);
        mailSender.send(mimeMessage);
    }

    /**
     * @throws Exception
     * @description: 发送freemarker解析字符串形式的邮件
     */
    @Test
    public void sendFreemarkerSimpleMail() throws Exception {
        Configuration templateConfiguration = new Configuration();
        String templateContent = "<html>\n" +
                "<body>\n" +
                "<h3>你好， ${name}, 这是一封模板邮件!</h3>\n" +
                "</body>\n" +
                "</html>\n";
        //模板Id
        String id = UUID.randomUUID().toString();
        //设置模板相关信息
        StringTemplateLoader loader = new StringTemplateLoader();
        loader.putTemplate(id, templateContent);
        templateConfiguration.setTemplateLoader(loader);

        Template template;
        StringWriter sw = null;
        String result;
        Map content = new HashMap<String, Object>();
        content.put("name", "周小黑");
        try {
            //获取模板
            template = templateConfiguration.getTemplate(id);
            sw = new StringWriter();
            //根据模板解析数据为普通html
            template.process(content, sw);
            result = sw.toString();
        } finally {
            IOUtils.closeQuietly(sw);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("zhouzhen@chinadaas.com");
        message.setTo("1012476676@qq.com");
        message.setSubject("测试邮件");
        message.setText(result);
        mailSender.send(message);
    }
}