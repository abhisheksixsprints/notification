package com.sixsprints.notification.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import com.sixsprints.notification.dto.MessageAuthDto;
import com.sixsprints.notification.dto.MessageDto;
import com.sixsprints.notification.service.NotificationService;

public class EmailServiceSmtp implements NotificationService {

  private MessageAuthDto emailAuth;

  public EmailServiceSmtp() {
  }

  public EmailServiceSmtp(MessageAuthDto emailAuth) {
    super();
    this.emailAuth = emailAuth;
  }

  @Override
  public Future<String> sendMessage(MessageDto emailDto) {
    return sendMessage(emailAuth, emailDto);
  }

  @Override
  public Future<String> sendMessage(MessageAuthDto emailAuthDto, MessageDto emailDto) {
    return Executors.newSingleThreadExecutor().submit(() -> send(emailAuthDto, emailDto));
  }

  private String send(MessageAuthDto emailAuthDto, MessageDto emailDto) {
    if (emailAuthDto == null) {
      throw new IllegalArgumentException("Email Auth cannot be null. Please create one before sending the mail.");
    }
    try {
      // Create the email message
      String from = emailAuthDto.getFromEmail();
      MultiPartEmail email = emailClient(emailAuthDto);
      email.setFrom(!isEmpty(from) ? from : emailAuthDto.getUsername(),
        emailAuthDto.getFrom());
      email.addTo(emailDto.getTo());
      email.setSubject(emailDto.getSubject());
      email.setMsg(emailDto.getContent());
      attach(emailDto, email);
      return email.send();
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private void attach(MessageDto emailDto, MultiPartEmail email) throws MalformedURLException, EmailException {
    if (emailDto.getAttachmentUrl() == null || emailDto.getAttachmentUrl().isEmpty()) {
      return;
    }
    EmailAttachment attachment = new EmailAttachment();
    attachment.setURL(new URL(emailDto.getAttachmentUrl()));
    attachment.setDisposition(EmailAttachment.ATTACHMENT);
    email.attach(attachment);
  }

  private MultiPartEmail emailClient(MessageAuthDto emailAuthDto) {
    MultiPartEmail email = new MultiPartEmail();
    email.setHostName(emailAuthDto.getHostName());
    email.setAuthenticator(new DefaultAuthenticator(emailAuthDto.getUsername(), emailAuthDto.getPassword()));
    email.setSSLOnConnect(emailAuthDto.isSslEnabled());
    email.setSslSmtpPort(emailAuthDto.getSslSmtpPort());
    return email;
  }

  private boolean isEmpty(String string) {
    return string == null || string.isEmpty();
  }

}
