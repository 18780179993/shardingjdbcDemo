package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import com.example.entity.DataSoruceProperties;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages="com.example.**")
@MapperScan(basePackages="com.example.mapper.**")
public class MyCodeShardingjdbcApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyCodeShardingjdbcApplication.class, args);
	}
}
