package com.clinic_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ClinicApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicApiApplication.class, args);
	}

}
