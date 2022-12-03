package com.spring.SpringBatchGradle.batch;




import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spring.SpringBatchGradle.domain.Dept;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * chunk 기반 
 * 청크 단위로 처리할 모든 레코드를 반복적으로 읽어오는 ItemReader
 * 필수는 아니지만 읽어들인 아이템을 처리하는 ItemProcessor
 * 아이템을 한 번에 기록하는 ItemWriter 의 세가지 컴포넌트로 구성
 * ItemWriter의 단일 호출은 물리적 쓰기를 일괄적으로 처리함으로써 IO 최적화를 이룸
 * 각 청크는 자체 트랜잭션으로 실행하며, 처리에 실패했다면 마지막으로 성공한 트랜잭션 이후부터 다시 시작 가능
 * 
 * */


@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob1 {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;
	
	
	private int chunkSize = 10; // 잘라서 해당 크기만큼 DB 처리 
	
	
	@Bean
	public Job JpaPageJob1_batchBuild() {
		return jobBuilderFactory.get("jpaPageJob1")
				.start(JpaPageJob1_step1()).build();
	}
	
	@Bean
	public Step JpaPageJob1_step1(){
		return stepBuilderFactory.get("jpaPageJob1_step1")
				.<Dept,Dept>chunk(chunkSize)
				.reader(jpaPageJob1_dbItemReader())
				.writer(jpaPageJob1_printItenWriter())
				.build();
	}
	
	@Bean
	public JpaPagingItemReader<Dept> jpaPageJob1_dbItemReader(){
		return new JpaPagingItemReaderBuilder<Dept>()
				.name("jpaPageJob1_dbItemReader")
				.entityManagerFactory(entityManagerFactory)
				.pageSize(chunkSize)
				.queryString("SELECT d FROM Dept d order by deptno asc")
				.build();
	}
	
	@Bean
	public ItemWriter<Dept> jpaPageJob1_printItenWriter(){
		return list ->{
			for(Dept dept : list) {
				log.debug(dept.toString());
			}
		};
	}
}
