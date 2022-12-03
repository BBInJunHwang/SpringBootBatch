package com.spring.SpringBatchGradle.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import com.spring.SpringBatchGradle.domain.Repository.DeptRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest		// testcase 선언
@Slf4j	
public class TestDeptRepository {

	@Autowired
	DeptRepository deptRepository;
	
	@Test			// 테스트 클래스 
	@Commit			// db 처리 후 commit 한다.
	public void dept01() {
		
		for(int i=1; i<101; i++) {
			log.debug("test start : " + i);
			deptRepository.save(new Dept(i,"dName_"+ String.valueOf(i), "loc_+"+ String.valueOf(i)));
		}
	}
	
}
