package com.example.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.A;
import com.example.mapper.AMapper;

@Service
public class TnsertService {
	@Autowired
	AMapper aMapper;
	
	public void insert(A a) {
		aMapper.insert(a);
	}

}
