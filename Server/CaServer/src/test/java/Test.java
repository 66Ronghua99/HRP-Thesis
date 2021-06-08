import com.ronghua.caserver.SpringbootApplication;
import com.ronghua.caserver.dao.CsrMapper;
import com.ronghua.caserver.entity.CsrEntity;
import com.ronghua.caserver.msgbody.SignRequest;
import com.ronghua.caserver.msgbody.SignRequestVerified;
import com.ronghua.caserver.service.CaService;
import com.ronghua.caserver.service.MailService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootApplication.class)
public class Test {

    @Autowired
    MailService mailService;

    @Autowired
    CsrMapper csrMapper;

    @Autowired
    CaService caService;

    @org.junit.Test
    public void sendMail(){
        mailService.sendMail("lalalafuck", "this is from the test", "893304167@qq.com");
    }

    @org.junit.Test
    public void getCsr(){
        List<CsrEntity> list = csrMapper.getCsrsByNameAndCode("Ronghua@kth.se", "1234");
        for (CsrEntity entity:list){
            System.out.println(entity.getTimeMillis());
            System.out.println(entity.getEncodedCsr());
        }
    }

    @org.junit.Test
    public void insertCsr(){
        SignRequest request = new SignRequest();
        request.setUsername("893304167@qq.com");
        request.setEncodedCsr("MIIBCDCBswIBADAsMQ4wDAYDVQQDDAVDaGluYTEMMAoGA1UECgwDS1RIMQwwCgYDVQQLDANOU1MwXDANBgkqhkiG9w0BAQEFAANLADBIAkEAmbufZ1rtCCD6077pZEAyUzMImmY8oQ3CZT30GeulP3kB1ORi36nTcPjae3f/u8+7o9ovNO2ZeWuR8zbxpg/c9QIDAQABoCIwIAYJKoZIhvcNAQkOMRMwETAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA0EAYB65w/yfgKRAwmTReawHm0qor8myYKtI5RXKhr4bdOdWXjHje9KMUSHkOw77kThfKHmltV/baIUyM3A5DufpvA==");
        caService.accountVerify(request);
    }

    @org.junit.Test
    public void verifySignReq(){
        SignRequestVerified signRequestVerified = new SignRequestVerified();
        signRequestVerified.setCode("APIO");
        signRequestVerified.setUsername("893304167@qq.com");
        caService.signCertificate(signRequestVerified);
    }

    @org.junit.Test
    public void deleteAllCert(){
        caService.deleteInvalidCert();
    }

}
