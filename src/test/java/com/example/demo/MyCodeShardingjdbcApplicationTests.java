package com.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.entity.A;
import com.example.service.TnsertService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=MyCodeShardingjdbcApplication.class)

public class MyCodeShardingjdbcApplicationTests {

	@Autowired
	TnsertService service;
	@Test
	public void test() {
		A a=new A();
		a.setId(1l);
		a.setNumb(9l);
		A a1=new A();
		a1.setId(2l);
		a1.setNumb(9l);
		service.insert(a);
		service.insert(a1);
	}

}
