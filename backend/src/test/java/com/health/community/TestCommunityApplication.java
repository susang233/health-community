package com.health.community;

import org.springframework.boot.SpringApplication;

public class TestCommunityApplication {

	public static void main(String[] args) {
		SpringApplication.from(CommunityApplication::main).run(args);
	}

}
