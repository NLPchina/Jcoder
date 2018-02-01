package org.nlpcn.jcoder.util.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;

/**
 * @author wang.kai@kuyun.com
 * @date 2014年10月27日
 */
public class EmailDao {

	private static final Logger LOG = LoggerFactory.getLogger(EmailDao.class);

	private String fromUser;
	private String fromPassword;
	private String smtpServer;

	/**
	 * @param fromUser
	 * @param fromPassword
	 * @param smtpServer
	 */
	public EmailDao(String fromUser, String fromPassword, String smtpServer) {
		this.fromUser = fromUser;
		this.fromPassword = fromPassword;
		this.smtpServer = smtpServer;
	}

	/**
	 * 向1到多个用户发送html格式的邮件。
	 *
	 * @param fromUser
	 * @param fromPassword
	 * @param smtpServer
	 * @param email_to_user
	 * @param title
	 * @param body
	 */
	public void sendHtml(String title, String body, String... toUsers) {
		send(toUsers, title, body, true);
	}

	/**
	 * 向1到多个用户发送文本格式的邮件。
	 *
	 * @param fromUser
	 * @param fromPassword
	 * @param smtpServer
	 * @param email_to_user
	 * @param title
	 * @param body
	 */
	public void sendText(String title, String body, String... toUsers) {
		send(toUsers, title, body, false);
	}

	/**
	 * 发送邮件， 支持1到多个用户，支持纯文本/html格式邮件。
	 *
	 * @param fromUser
	 * @param fromPassword
	 * @param smtpServer
	 * @param toUserList
	 * @param title
	 * @param body
	 * @param isHtml
	 */
	public void send(String[] toUserList, String title, String body, boolean isHtml) {
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpServer);
		props.put("mail.smtp.auth", "true");
		Session s = Session.getInstance(props);

		MimeMessage message = new MimeMessage(s);

		// 给消息对象设置发件人/收件人/主题/发信时间
		try {
			InternetAddress from = new InternetAddress(fromUser);
			message.setFrom(from);
			LinkedList<InternetAddress> to_list = new LinkedList<InternetAddress>();
			InternetAddress[] to_array = new InternetAddress[toUserList.length];
			for (String email_to_user : toUserList) {
				to_list.add(new InternetAddress(email_to_user));
			}
			message.setRecipients(Message.RecipientType.TO, to_list.toArray(to_array));
			message.setSubject(title);
			message.setSentDate(new Date());

			if (isHtml) {
				// 给消息对象设置内容
				BodyPart mdp = new MimeBodyPart();// 新建一个存放信件内容的BodyPart对象
				mdp.setContent(body, "text/html;charset=utf-8");// 给BodyPart对象设置内容和格式/编码方式
				Multipart mm = new MimeMultipart();// 新建一个MimeMultipart对象用来存放BodyPart对
				// 象(事实上可以存放多个)
				mm.addBodyPart(mdp);// 将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)
				message.setContent(mm);// 把mm作为消息对象的内容
			} else {
				message.setText(body, "utf-8");
			}

			message.saveChanges();
			Transport transport = s.getTransport("smtp");
			transport.connect(smtpServer, fromUser, fromPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * @return the fromUser
	 */
	public String getFromUser() {
		return fromUser;
	}

	/**
	 * @param fromUser the fromUser to set
	 */
	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	/**
	 * @return the fromPassword
	 */
	public String getFromPassword() {
		return fromPassword;
	}

	/**
	 * @param fromPassword the fromPassword to set
	 */
	public void setFromPassword(String fromPassword) {
		this.fromPassword = fromPassword;
	}

	/**
	 * @return the smtpServer
	 */
	public String getSmtpServer() {
		return smtpServer;
	}

	/**
	 * @param smtpServer the smtpServer to set
	 */
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}


}
