package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * @ClassName: MailUtils
 * @Description: 邮件发送工具类
 * @version: V1.0
 */
public class MailUtils {

    private static final Properties props = new Properties();

    // 使用静态代码块加载配置文件
    static {
        try (InputStream is = MailUtils.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (is == null) {
                throw new RuntimeException("未找到 mail.properties 配置文件");
            }
            props.load(is);
        } catch (Exception e) {
            throw new RuntimeException("加载 mail.properties 配置文件失败", e);
        }
    }

    /**
     * 发送邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @throws MessagingException
     */
    public static void sendMail(String to, String subject, String content) throws MessagingException {
        // 添加更详细的属性以禁用SSL/TLS
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true");

        // 1. 获取与邮件服务器的会话
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 从配置文件中获取发件人邮箱账号和授权码
                return new PasswordAuthentication(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"));
            }
        });

        // 2. 创建邮件消息
        MimeMessage message = createMimeMessage(session, props.getProperty("mail.smtp.user"), to, subject, content);

        // 3. 发送邮件
        Transport.send(message);
    }

    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param session     和服务器交互的会话
     * @param sendMail    发件人邮箱
     * @param receiveMail 收件人邮箱
     * @param subject     邮件主题
     * @param content     邮件内容
     * @return
     * @throws MessagingException
     */
    private static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail, String subject, String content) throws MessagingException {
        try {
            // 1. 创建一封邮件
            MimeMessage message = new MimeMessage(session);
            // 2. From: 发件人
            message.setFrom(new InternetAddress(sendMail, props.getProperty("mail.sender.nickname", "SCUMDB"), "UTF-8"));
            // 3. To: 收件人（可以增加多个收件人、抄送、密送）
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "尊敬的用户", "UTF-8"));
            // 4. Subject: 邮件主题
            message.setSubject(subject, "UTF-8");
            // 5. Content: 邮件正文（可以使用HTML标签）
            message.setContent(content, "text/html;charset=UTF-8");
            // 6. 设置发件时间
            message.setSentDate(new java.util.Date());
            // 7. 保存设置
            message.saveChanges();
            return message;
        } catch (Exception e) {
            throw new MessagingException("创建邮件失败", e);
        }
    }
}
