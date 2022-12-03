package com.spring.SpringBatchGradle.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * Tasklet은 계속 진행 or 끝낼지 2가지만 제공 
 * chunk와 달리 RPW 로직이 나눠지는게 아닌, 1회성 or RPW가 한번에 모아놓은 비지니스 로직 사용
 * chunk와 달리 메타 테이블에 read,write count 안남음 
 * -> 직접 구현해야함 
 * 
 * RepeatStatus.FINISHED	- 처리의 성공 여부 관계없이 태스크릿을 완료하고 다음 처리를 이어서 하겠다
 * RepeatStatus.CONTINUABLE - 스프링 배치에게 해당 태스크릿을 다시 실행
 * 
 * */

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TaskletJob {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	
	
	@Bean
	public Job taskletJob_batchBuild() {
		return jobBuilderFactory.get("taskletJob")
				.start(taskletJob_step1())
				.next(taskletJob_step2(null))	// next 이용해서 step 간 연결한다.
				.build();
	}
	
	
	/**
	 * contribution - 현재 단계 실행을 업데이트하기 위해 다시 전달되는 변경 가능한 상태
     * chunkContext - 호출 간에는 공유되지만 재시작 간에는 공유되지 않는 속성
	 * */
	@Bean
	public Step taskletJob_step1(){
		return stepBuilderFactory.get("taskletJob_step1")
				.tasklet((contribution,chunkContext)->{
					log.debug("-> job -> [step1]");
					return RepeatStatus.FINISHED;
				}).build();
	}
	
	@Bean
	@JobScope	// late binding - Bean 생성시점이 애플리케이션 구동이 아닌 지정된 scope가 실행되는 시점 -> 로직이 실행되는 시점에 값들 저장 후 다음 step에 전달 or 실패지점 재처리
	public Step taskletJob_step2(@Value("#{jobParameters[date]}") String date){
		return stepBuilderFactory.get("taskletJob_step2")
				.tasklet((a,b)->{
					log.debug("-> step1 -> [step2]" + date);
					return RepeatStatus.FINISHED;
				}).build();
	}
	
}
