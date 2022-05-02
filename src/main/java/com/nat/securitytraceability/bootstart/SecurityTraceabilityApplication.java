package com.nat.securitytraceability.bootstart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = {"com.nat.securitytraceability"})
@EnableSwagger2
@EnableScheduling
@ServletComponentScan("com.nat.securitytraceability")
@MapperScan("com.nat.securitytraceability.**.mapper")
public class SecurityTraceabilityApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityTraceabilityApplication.class, args);
    }
}
