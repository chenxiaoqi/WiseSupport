package com.wisesupport.cloud.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableConfigServer
@EnableEurekaServer
public class CloudConfigServer {

	public static void main(String[] args) {
		SpringApplication.run(CloudConfigServer.class, args);
	}

}
