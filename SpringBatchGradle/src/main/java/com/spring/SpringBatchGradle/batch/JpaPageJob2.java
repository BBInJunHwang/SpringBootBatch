package com.spring.SpringBatchGradle.batch;




import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.spring.SpringBatchGradle.domain.Dept;
import com.spring.SpringBatchGradle.domain.Dept2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 
 * 한번 성공한 배치는 version 다르거나 파라미터가 다르지 않는한 실행하지 않는다.
 * 
 * 스프링 배치는 자체적으로 스케줄링 기능이 없음
 * 외부에서 cronTab/Jeknins 사용해서 JOB 파라미터 넘겨줘야함
 * 테스트를 위해서 program argument 통해 job name과 job parameter 전달 
 * 
 * 
 * JobInstance 		- Job 실행단위, Job 실행시 하나의 JobInstance 생성
 * 			     	ex) 오전,오후 Job 실행시 각각 생성되며, 다시 오전 Job 실행 시 인스턴스 생성x 오전 실행 배치만 다시 실행
 * 
 * Job Execution 	- JobInstance의 실행시도 대한 객체
 * 					ex) 오전 인스턴스 실패시 실패지점 부터 다시실행되며, 이경우 실패 인스턴스, 성공 인스턴스 새로생김 히스토리용
 * 
 * Step				- Job 배치처리 정의, 순차적으로 캡슐화, Job은 한개이상 Step 필요 
 * 					Tasklet과 chunk 처리방식 지원
 * 
 * Step Execution	- Step 실행시도 대한 객체, Job이 여러개 Step으로 구성시 이전 Step 실패시 다음단계 실행x, stepExecution 생성x
 * 					read ,write ,commit 수 등 저장
 * 
 * Execution Context - Job에서 데이터를 공유할 수 있는 데이터 저장소, Job, Step 2가지 제공
 * 					JobExecution Context는 Commit시점 저장, StepExecution Context는 실행 사이 저장
 * 					Execution Context 통해 Step간 데이터 공유 및 Job 실패시 마지막 실행값 재구성 가능 
 * 
 * JobRepository	 - 모든 Batch 처리 정보가짐, Job 실행시 job,step Execution 생성
 * 					 Execution 정보를 저장하고 조회
 * 
 * JobLauncher		 - Job과 JobParameters 사용해 Job 실행 객체
 * 
 * ItemReader		 - Step에서 item 읽는 인터페이스 
 * 
 * ItemWriter		 - 처리된 데이터를 insert,update,send(MQ로 전달) 할때 사용
 * 						Item을 chunk 단위로 묶어 처리
 * 
 * ItemProcessor	 - Reader에서 읽은 item 처리
 * 					 필수요소는 아니며, 데이터 가공 등 비지니스 로직
 * 
 * 
 * 
 * 
 * 
 * 
 * */







@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob2 {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	
	
	private int chunkSize = 20; // 잘라서 해당 크기만큼 DB 처리 
	
	
	@Bean
	public Job JpaPageJob2_batchBuild() {
		return jobBuilderFactory.get("jpaPageJob2")
				.start(JpaPageJob2_step1()).build();
	}
	
	@Bean
	public Step JpaPageJob2_step1(){
		return stepBuilderFactory.get("jpaPageJob2_step2")
				.<Dept,Dept2>chunk(chunkSize)
				.reader(jpaPageJob2_dbItemReader())
				.processor(jpaPageJob2_processor())
				.writer(jpaPageJob2_dbItenWriter())
				.build();
	}
	

	@Bean
	public JpaPagingItemReader<Dept> jpaPageJob2_dbItemReader(){
		return new JpaPagingItemReaderBuilder<Dept>()
				.name("jpaPageJob2_dbItemReader")
				.entityManagerFactory(entityManagerFactory)
				.pageSize(chunkSize)
				.queryString("SELECT d FROM Dept d order by deptno asc")
				.build();
	}
	
	
	private ItemProcessor<Dept ,Dept2> jpaPageJob2_processor() {
		return dept ->{
			return new Dept2(dept.getDeptNo(),"NEW_"+ dept.getDName(), "NES_" + dept.getLoc());
		};
	}
	
	
	@Bean
	public JpaItemWriter<Dept2> jpaPageJob2_dbItenWriter(){
		JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();		// JpaItemWriter 사용시 영속성 관리를 위한 EntityManager 전달 필요
		jpaItemWriter.setEntityManagerFactory(entityManagerFactory);	// 넘어온 Entity를 데이터베이스에 반영
																		// Entity 클래스를 제네릭 타입 -> 넘어온 Item을 그대로 entityManger.merge()로 테이블에 반영
		return jpaItemWriter;
	}
}
