package org.hrjin.servicebroker.oracle.config;

/**
 * Created by hrjin on 2018-03-26.
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan
public class OracleApplication {

    public static void main(String[] args) {
        SpringApplication.run(OracleApplication.class, args);
    }

}
