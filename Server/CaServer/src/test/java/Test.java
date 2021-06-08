import com.ronghua.caserver.SpringbootApplication;
import com.ronghua.caserver.service.MailService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringbootApplication.class)
public class Test {

    @Autowired
    MailService mailService;

    @org.junit.Test
    public void sendMail(){
        mailService.sendMail("lalalafuck", "this is from the test", "893304167@qq.com");
    }

}
