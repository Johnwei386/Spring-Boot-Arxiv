package com.snail.arxiv;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(value = "com.snail.arxiv.mapper")
@SpringBootApplication
public class ArxivCollectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArxivCollectionApplication.class, args);
	}

}
