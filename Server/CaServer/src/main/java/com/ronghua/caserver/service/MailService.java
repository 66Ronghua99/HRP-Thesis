package com.ronghua.caserver.service;

import com.ronghua.caserver.entity.MailEntity;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class MailService {
    private Properties properties;
    private Authenticator authenticator;
    private InternetAddress internetAddress;
    private long timeStamp;


    public MailService(Properties properties, Authenticator authenticator, InternetAddress internetAddress) {
        this.properties = properties;
        this.authenticator = authenticator;
        this.internetAddress = internetAddress;
        timeStamp = System.currentTimeMillis();
    }

    public void sendMail(String subject, String content, String receiver){
        sendMail(subject, content, Collections.singletonList(receiver));
    }

    public void sendMail(String subject, String content, List<String> receivers){
        MailEntity request = new MailEntity();
        request.setSubject(subject);
        request.setContent(content);
        request.setReceivers(receivers);
        sendMail(request);
    }

    public void sendMail(MailEntity request) {

        Session session = Session.getInstance(properties, authenticator);
        try {
            Message message = new MimeMessage(session);
            message.setSubject(request.getSubject());
            message.setContent(request.getContent(), "text/html; charset=UTF-8");
            message.setFrom(internetAddress);
            for (String address : request.getReceivers()) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
            }

            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
