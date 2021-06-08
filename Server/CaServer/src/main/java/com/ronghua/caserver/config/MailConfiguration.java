package com.ronghua.caserver.config;

import com.ronghua.caserver.service.MailService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "mail")//only ConfigurationProperties could reflect to those name with 'camel', '-' and '_'
//with prefix, we can define and match properties more precisely
@PropertySource(value = {"classpath:mail.properties"}) //default classpath would be application.properties, but could be set to mail.properties
public class MailConfiguration {
    public final static String GMAIL_CONFIG = "gmail.config";
    public final static String _126_CONFIG = "126.config";
    private String platform;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST,
            proxyMode = ScopedProxyMode.TARGET_CLASS) //Request Scope: Bean is injected during each http request. Being singleton during this request
    public MailService mailService() throws UnsupportedEncodingException {
        System.out.println("------------New mailService bean injected--------------");
        return "126.config".equals(platform)? otsMailService() : gmailMailService();
    }

//    @Bean(name = GMAIL_CONFIG)
    public MailService gmailMailService() throws UnsupportedEncodingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", gmailHost);
        properties.put("mail.smtp.port", gmailPort);
        properties.put("mail.smtp.auth", String.valueOf(authEnabled));
        properties.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnabled));
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(gmailUserAddress, gmailUserPwd);
            }
        };
        InternetAddress internetAddress = new InternetAddress(gmailUserAddress, userDisplayName);
        return new MailService(properties, authenticator, internetAddress);
    }

//    @Bean(name = _126_CONFIG)
    public MailService otsMailService() throws UnsupportedEncodingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", otsHost);
        properties.put("mail.smtp.port", otsPort);
        properties.put("mail.smtp.auth", String.valueOf(authEnabled));
        properties.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnabled));
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(otsUserAddress, otsUserPwd);
            }
        };
        InternetAddress internetAddress = new InternetAddress(otsUserAddress, userDisplayName);
        return new MailService(properties, authenticator, internetAddress);
    }

    private String gmailHost;
    private int gmailPort;
    private String gmailUserAddress;
    private String gmailUserPwd;

    private String otsHost;
    private int otsPort;
    private String otsUserAddress;
    private String otsUserPwd;

    private boolean authEnabled;
    private boolean starttlsEnabled;
    private String userDisplayName;

    public String getGmailHost() {
        return gmailHost;
    }

    public void setGmailHost(String gmailHost) {
        this.gmailHost = gmailHost;
    }

    public int getGmailPort() {
        return gmailPort;
    }

    public void setGmailPort(int gmailPort) {
        this.gmailPort = gmailPort;
    }

    public String getGmailUserAddress() {
        return gmailUserAddress;
    }

    public void setGmailUserAddress(String gmailUserAddress) {
        this.gmailUserAddress = gmailUserAddress;
    }

    public String getGmailUserPwd() {
        return gmailUserPwd;
    }

    public void setGmailUserPwd(String gmailUserPwd) {
        this.gmailUserPwd = gmailUserPwd;
    }

    public String getOtsHost() {
        return otsHost;
    }

    public void setOtsHost(String otsHost) {
        this.otsHost = otsHost;
    }

    public int getOtsPort() {
        return otsPort;
    }

    public void setOtsPort(int otsPort) {
        this.otsPort = otsPort;
    }

    public String getOtsUserAddress() {
        return otsUserAddress;
    }

    public void setOtsUserAddress(String otsUserAddress) {
        this.otsUserAddress = otsUserAddress;
    }

    public String getOtsUserPwd() {
        return otsUserPwd;
    }

    public void setOtsUserPwd(String otsUserPwd) {
        this.otsUserPwd = otsUserPwd;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }

    public boolean isStarttlsEnabled() {
        return starttlsEnabled;
    }

    public void setStarttlsEnabled(boolean starttlsEnabled) {
        this.starttlsEnabled = starttlsEnabled;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }
}


