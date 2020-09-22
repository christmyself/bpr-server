package cn.himavat.bpr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class BprApplication {
    public static void main(String[] args) {
        SpringApplication.run(BprApplication.class, args);
        log.info("Application has start up");
    }

}
