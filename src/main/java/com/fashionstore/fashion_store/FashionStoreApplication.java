package com.fashionstore.fashion_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FashionStoreApplication {

	public static void main(String[] args) {

		SpringApplication.run(FashionStoreApplication.class, args);
	}
}
