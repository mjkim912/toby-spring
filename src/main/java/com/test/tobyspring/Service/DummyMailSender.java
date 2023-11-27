package com.test.tobyspring.Service;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * org.springframework.mail.javamail.JavaMailSenderImpl 을 사용하는 대신 테스트하는 용이다.
 *
 */
public class DummyMailSender implements MailSender {
	public void send(SimpleMailMessage mailMessage) throws MailException {
		
	}

	public void send(SimpleMailMessage[] mailMessage) throws MailException {
		
	}

}
