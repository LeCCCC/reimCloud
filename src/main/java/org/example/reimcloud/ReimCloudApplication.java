package org.example.reimcloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.reimcloud.mapper")
public class ReimCloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReimCloudApplication.class, args);
    }

}
