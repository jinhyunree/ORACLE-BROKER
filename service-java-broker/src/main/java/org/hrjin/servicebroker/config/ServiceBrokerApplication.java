package org.hrjin.servicebroker.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan
public class ServiceBrokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBrokerApplication.class, args);
    }

}