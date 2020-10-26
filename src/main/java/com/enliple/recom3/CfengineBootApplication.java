package com.enliple.recom3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CfengineBootApplication {
	public static void main(String[] args) {
		SpringApplication application = new SpringApplicationBuilder()
				.sources(CfengineBootApplication.class)
				.listeners(new ApplicationPidFileWriter("/home/users/rpapp/home/pid/cfBoot.pid"))
				.build();
		application.run(args);
	}

}
