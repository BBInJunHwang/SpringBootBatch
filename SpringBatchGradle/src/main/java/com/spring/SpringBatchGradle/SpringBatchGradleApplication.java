package com.spring.SpringBatchGradle;


import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



/**
 * Spring web 프로젝트가 아니기 때문에
 * 배치프로젝트는 실행 후 배치가 있으면 -> 수행 후 종료, 없으면 바로 종료
 * 
 * properties -> run/debug arguments -> --job.name=taskletJob v=1..v=2... 현재는 taskletJob name과 version v 값을 계속 넣어준다
 * 넣지 않으면 한번 돈 배치는 다시 돌지 않는다.
 * 
 * */
@EnableBatchProcessing	// 해당 프로젝트는 배치입니다 설정
@SpringBootApplication
public class SpringBatchGradleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchGradleApplication.class, args);
	}

}
