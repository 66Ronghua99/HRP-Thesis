package com.ronghua.caserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MailAuthService {

    @Autowired
    private MailService mailService;

    public String sendAuthMail(String username){
        String content = "Hi, this is the authentication code: \n   %s  \n Please verify yourself to the server within five minute";
        String code = randomVerifyCode();
        content = String.format(content, code);
        mailService.sendMail("Verification Code", content, username);
        return code;
    }


    private String randomVerifyCode(){
        String str="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb=new StringBuilder(4);
        for(int i=0;i<4;i++)
        {
            char ch=str.charAt(new Random().nextInt(str.length()));
            sb.append(ch);
        }
        return sb.toString();
    }

}
