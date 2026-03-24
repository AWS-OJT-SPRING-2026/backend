package ojt.aws.educare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EduCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduCareApplication.class, args);
    }

}
