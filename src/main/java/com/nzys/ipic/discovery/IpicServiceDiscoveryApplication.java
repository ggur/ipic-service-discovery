package com.nzys.ipic.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class IpicServiceDiscoveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(IpicServiceDiscoveryApplication.class, args);
	}

}
