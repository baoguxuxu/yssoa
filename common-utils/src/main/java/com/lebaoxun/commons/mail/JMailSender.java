package com.lebaoxun.commons.mail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import com.lebaoxun.commons.exception.I18nMessageException;
 
/**
 * 简单邮件发送器，可单发，群发。
 * 
 * @author cqy
 * 
 */
public class JMailSender {
 
    /**
     * 发送邮件的props文件
     */
    private final transient Properties props = System.getProperties();
    /**
     * 邮件服务器登录验证
     */
    protected transient MailAuthenticator authenticator;
 
    /**
     * 邮箱session
     */
    protected transient Session session;
 
    /**
     * 初始化邮件发送器
     * 
     * @param smtpHostName
     *                SMTP邮件服务器地址
     * @param username
     *                发送邮件的用户名(地址)
     * @param password
     *                发送邮件的密码
     */
    public JMailSender(final String smtpHostName, final String username,
        final String password) {
    	init(username, password, smtpHostName);
    }
 
    /**
     * 初始化邮件发送器
     * 
     * @param username
     *                发送邮件的用户名(地址)，并以此解析SMTP服务器地址
     * @param password
     *                发送邮件的密码
     */
    public JMailSender(final String username, final String password) {
	    //通过邮箱地址解析出smtp服务器，对大多数邮箱都管用
	    final String smtpHostName = "smtp." + username.split("@")[1];
	    init(username, password, smtpHostName);
    }
 
    /**
     * 初始化
     * 
     * @param username
     *                发送邮件的用户名(地址)
     * @param password
     *                密码
     * @param smtpHostName
     *                SMTP主机地址
     */
    private void init(String username, String password, String smtpHostName) {
	    // 初始化props
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.host", smtpHostName);
	    // 验证
	    authenticator = new MailAuthenticator(username, password);
	    // 创建session
	    session = Session.getInstance(props, authenticator);
    }
 
    /**
     * 发送邮件
     * 
     * @param recipient
     *                收件人邮箱地址
     * @param subject
     *                邮件主题
     * @param content
     *                邮件内容
     * @throws AddressException
     * @throws MessagingException
     */
    public void send(String recipient, String subject, Object content) throws I18nMessageException {
    	try {
		    // 创建mime类型邮件
		    final MimeMessage message = new MimeMessage(session);
		    // 设置发信人
		    message.setFrom(new InternetAddress(authenticator.getUsername()));
		    // 设置收件人
		    message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
		    // 设置主题
		    message.setSubject(subject);
		    // 设置邮件内容
		    message.setContent(content.toString(), "text/html;charset=utf-8");
		    // 发送
		    Transport.send(message);
	    } catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new I18nMessageException("－1","邮件发送失败",e);
		}
    }
 
    /**
     * 群发邮件
     * 
     * @param recipients
     *                收件人们
     * @param subject
     *                主题
     * @param content
     *                内容
     * @throws AddressException
     * @throws MessagingException
     */
    public void send(List<String> recipients, String subject, Object content)
        throws I18nMessageException {
	    // 创建mime类型邮件
	    final MimeMessage message = new MimeMessage(session);
	    // 设置发信人
	    try {
			message.setFrom(new InternetAddress(authenticator.getUsername()));
			// 设置收件人们
			final int num = recipients.size();
			InternetAddress[] addresses = new InternetAddress[num];
			for (int i = 0; i < num; i++) {
				addresses[i] = new InternetAddress(recipients.get(i));
			}
			message.setRecipients(RecipientType.TO, addresses);
			// 设置主题
			message.setSubject(subject);
			// 设置邮件内容
			message.setContent(content.toString(), "text/html;charset=utf-8");
			// 发送
			Transport.send(message);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new I18nMessageException("－1","邮件发送失败",e);
		}
    }
 
    /**
     * 发送邮件
     * 
     * @param recipient
     *                收件人邮箱地址
     * @param mail
     *                邮件对象
     * @throws AddressException
     * @throws MessagingException
     */
    public void send(String recipient, JMailTemplate mail){
    	send(recipient, mail.getSubject(), mail.getContent());
    }
    
    /**
	 * 
	 * @param recipient 收件邮箱地址
	 * @param subject   主题
	 * @param content   内容
	 * @param file      附件
	 * @throws AddressException
	 * @throws MessagingException
	 */
	public void sendFile(String recipient, String subject, Object content, String url){
		final MimeMessage message = new MimeMessage(this.session);
		try {
			message.setFrom(new InternetAddress(authenticator.getUsername()));
			message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
			message.setSubject(subject);
			message.setContent(content.toString(), "text/html;charset=utf-8");
			
			// 根据文件名获取数据源
			// 发送附件
			Multipart multipart = new MimeMultipart();
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			DataSource source = null;
			source = new URLDataSource(new URL(url));
			String fileName = url.substring(url.lastIndexOf("/")+1);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(fileName);
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);
			message.saveChanges();
			Transport.send(message);
		} catch (MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
    /**
     * 群发邮件
     * 
     * @param recipients
     *                收件人们
     * @param mail
     *                邮件对象
     * @throws AddressException
     * @throws MessagingException
     */
    public void send(List<String> recipients, JMailTemplate mail)
        throws AddressException, MessagingException {
    	send(recipients, mail.getSubject(), mail.getContent());
    }
    
	public static void main(String[] args) {
    	JMailSender sender = new JMailSender("smtp.mxhichina.com","fengyr@i-shancan.com","1234.FYR");
		sender.send("caiqy@i-shancan.com", "测试", "得分手段");
	}
    
}