package it.l_soft.wediConf.utils;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.log4j.Logger;

import it.l_soft.wediConf.rest.ApplicationProperties;

public class Mailer
{
	static final Logger log = Logger.getLogger(Mailer.class);
	static ApplicationProperties ap = ApplicationProperties.getInstance();

	public static void sendMail(String mailTo, String mailBody)
	{
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", ap.getMailSmtpHost());
		props.put("mail.smtp.port", "25");

		log.debug("Connecting to mail server '" + ap.getMailSmtpHost() + "' on port 25");
		// Get the Session object.
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(ap.getMailUser(), ap.getMailPassword());
			}
		});

		try {
			// Create a default MimeMessage object.
			Message message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(ap.getMailFrom()));

			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo));
			message.setRecipients(Message.RecipientType.BCC,InternetAddress.parse("osvaldo.lucchini@wedi.it"));

			// Set Subject: header field
			message.setSubject("Conferma attivazione utente configuratore wedi");

			// Send the actual HTML message, as big as you like
			message.setContent(mailBody, "text/html");

			// Send message
			log.trace("Ready to send message via Transport.send");
			Transport.send(message);

			log.trace("Sent message successfully....");

		} 
		catch (MessagingException e) 
		{
			log.debug("Exception " + e.getMessage(), e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
