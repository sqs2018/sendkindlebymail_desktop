package com.sqs.util;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Properties;

/**
 * 163邮件发送工具类
 * 整合了文本、HTML、附件、内嵌图片等所有功能
 */
public class EmailUtil163 {

    private static final String SMTP_HOST = "smtp.163.com";
    private static final String SMTP_PORT = "465";

    private String fromEmail;
    private String authCode;
    private Session session;

    /**
     * 构造函数
     * @param fromEmail 发件人163邮箱
     * @param authCode  授权码
     */
    public EmailUtil163(String fromEmail, String authCode) {
        this.fromEmail = fromEmail;
        this.authCode = authCode;
        initSession();
    }

    /**
     * 初始化Session
     */
    private void initSession() {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", SMTP_HOST);
        props.setProperty("mail.smtp.port", SMTP_PORT);
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.ssl.enable", "true");
        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, authCode);
            }
        });
        // 调试模式，生产环境请关闭
        // session.setDebug(true);
    }

    /**
     * 发送简单文本邮件
     */
    public boolean sendText(String toEmail, String subject, String content) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建基础邮件对象
     */
    private MimeMessage createBaseMessage(String toEmail, String subject) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(subject);
        return message;
    }

    /**
     * 创建文本正文部分
     */
    private MimeBodyPart createTextPart(String content) throws MessagingException {
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(content, "UTF-8");
        return textPart;
    }

    /**
     * 创建附件部分
     */
    private MimeBodyPart createAttachmentPart(File file, String displayName) throws Exception {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(file);
        attachmentPart.setDataHandler(new DataHandler(fds));
        attachmentPart.setFileName(MimeUtility.encodeText(displayName));
        return attachmentPart;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(lastDot + 1).toUpperCase();
        }
        return "未知类型";
    }

    /**
     * 设置调试模式
     */
    public void setDebug(boolean debug) {
        session.setDebug(debug);
    }

    /**
     * 方法1：发送多个附件（使用File数组）
     * @param toEmail 收件人
     * @param subject 主题
     * @param emailBody 正文
     * @param attachments 附件文件数组
     * @return 是否成功
     */
    public boolean sendMultipleAttachments(String toEmail, String subject,
                                           String emailBody, File[] attachments) {
        try {
            MimeMessage message = createBaseMessage(toEmail, subject);
            Multipart multipart = new MimeMultipart();

            // 添加正文
            multipart.addBodyPart(createTextPart(emailBody));

            // 添加多个附件
            if (attachments != null && attachments.length > 0) {
                for (File file : attachments) {
                    if (file.exists() && file.isFile()) {
                        multipart.addBodyPart(createAttachmentPart(file, file.getName()));
                        System.out.println("添加附件: " + file.getName() + " (" + file.length() + " 字节)");
                    } else {
                        System.out.println("警告: 文件不存在或不是文件 - " + file.getPath());
                    }
                }
            }

            message.setContent(multipart);
            Transport.send(message);

            System.out.println("邮件发送成功！共添加 " + attachments.length + " 个附件");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 发送带TXT附件的邮件
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param emailBody 邮件正文内容
     * @param txtFile TXT文件对象
     * @return 发送成功返回true，失败返回false
     */
    public boolean sendTxtAttachment(String toEmail, String subject,
                                     String emailBody, File txtFile) {
        try {
            // 1. 创建邮件
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);

            // 2. 创建 multipart 对象
            Multipart multipart = new MimeMultipart();

            // 3. 添加邮件正文
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(emailBody, "UTF-8");
            multipart.addBodyPart(textPart);

            // 4. 添加TXT附件
            if (txtFile != null && txtFile.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();

                // 使用FileDataSource读取TXT文件
                FileDataSource fds = new FileDataSource(txtFile);
                attachmentPart.setDataHandler(new DataHandler(fds));

                // 设置附件文件名（解决中文乱码）
                attachmentPart.setFileName(MimeUtility.encodeText(txtFile.getName()));

                multipart.addBodyPart(attachmentPart);

                System.out.println("添加附件: " + txtFile.getAbsolutePath());
                System.out.println("文件大小: " + txtFile.length() + " 字节");
            } else {
                System.out.println("警告: TXT文件不存在或为空");
            }

            // 5. 设置邮件内容
            message.setContent(multipart);

            // 6. 发送邮件
            Transport.send(message);

            System.out.println("邮件发送成功！");
            return true;

        } catch (Exception e) {
            System.out.println("邮件发送失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送带附件的邮件
     */
    public boolean sendWithAttachments(String toEmail, String subject,
                                       String content, boolean isHtml,
                                       File[] attachments) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();

            // 添加正文
            MimeBodyPart contentPart = new MimeBodyPart();
            if (isHtml) {
                contentPart.setContent(content, "text/html;charset=UTF-8");
            } else {
                contentPart.setText(content);
            }
            multipart.addBodyPart(contentPart);

            // 添加附件
            if (attachments != null) {
                for (File file : attachments) {
                    if (file.exists() && file.isFile()) {
                        MimeBodyPart attachmentPart = new MimeBodyPart();
                        FileDataSource fds = new FileDataSource(file);
                        attachmentPart.setDataHandler(new DataHandler(fds));
                        attachmentPart.setFileName(MimeUtility.encodeText(file.getName()));
                        multipart.addBodyPart(attachmentPart);
                    }
                }
            }

            message.setContent(multipart);
            Transport.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送带内嵌图片的HTML邮件
     */
    public boolean sendHtmlWithImage(String toEmail, String subject,
                                     String htmlContent, String contentId, File imageFile) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart("related");

            // HTML文本部分
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html;charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // 内嵌图片部分
            if (imageFile != null && imageFile.exists()) {
                MimeBodyPart imagePart = new MimeBodyPart();
                FileDataSource fds = new FileDataSource(imageFile);
                imagePart.setDataHandler(new DataHandler(fds));
                imagePart.setContentID(contentId);
                imagePart.setDisposition(MimeBodyPart.INLINE);
                multipart.addBodyPart(imagePart);
            }

            message.setContent(multipart);
            Transport.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 使用示例
    public static void main(String[] args) {
        EmailUtil163 emailUtil = new EmailUtil163("XXX@163.com", "XXX");

        // 发送文本邮件
        //emailUtil.sendText("recipient@example.com", "测试邮件", "Hello World!");

        // 发送带附件的邮件
        /*File[] files = {new File("D:/a.txt")};
        emailUtil.sendWithAttachments("18600153020@kindle.com", "2026-02-28",
                "<h1>附件内容</h1>", true, files);*/
        emailUtil.sendTxtAttachment("XXX@kindle.com", DateUtil.getCurrentDateTime(),
                "<h1>附件内容</h1>", new File("D:/a.txt"));
    }
}