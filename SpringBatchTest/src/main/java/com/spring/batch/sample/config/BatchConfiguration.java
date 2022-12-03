package com.spring.batch.sample.config;


import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.spring.batch.sample.listener.JobCompletionNotificationListener;
import com.spring.batch.sample.model.Person;
import com.spring.batch.sample.processor.PersonItemProcessor;

import lombok.extern.slf4j.Slf4j;

/**
 * Job - 배치처리과정 하나 단위로 표현
 * itemProcessor - 데이터 가공 및 필터링 (필수는 아님), 유효하지 않는경우 null 반환
 *               - writer에서 구현가능하지만, reader,writer 등 별도로 분리해 비지니스 코스 섞임 방지
 *               - 변환과 필터 기능 -> reader 에서 읽은 데이터 타입변경해 writer 전달
 *               				-> reader 에서 읽은 데이터 writer로 넘겨줄지 말지 판단 -> null 인경우 writer로 전달 x 
 * 
 * JdbcBatchItemWriter - ORM 사용 아닌경우 대부분 사용, JDBC의 Batch 기능 이용해 한번에 query를 모은 후 DB로 전달 후 내부에서 모은 쿼리실행              
 * 					   - 어플리케이션과 데이터베이스 간에 데이터를 주고 받는 회수를 최소화 하여 성능 향상
 * 
 * RPW 순으로 진행 Read Processor Writer
 * 
 * 
 * Chunk 		- 각 커밋 사이에 처리되는 row 수, Chunk 지향 처리란 한 번에 하나씩 데이터를 읽어 Chunk라는 덩어리를 만든 뒤, Chunk 단위로 트랜잭션 처리
 * 				- Chunk 단위로 트랜잭션을 수행하기 때문에 실패할 경우엔 해당 Chunk 만큼만 롤백, 이전에 커밋된 트랜잭션 범위까지는 반영
 * 
 * */


@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
	@Autowired 
	public JobBuilderFactory jobBuilderFactory;		// JobBuilder 생성가능, Job 만드는 용도?
	
    @Autowired 
    public StepBuilderFactory stepBuilderFactory;	// stepBuilder ? 생성? -> factory가 여러 기능 제공?
    												// StepBuilderFactory 할때 StepBuilderFactory.class 열어보면 transactionManager를 주입 받은 후 
    												// stepBuilderFactory.get("step1") 이런식으로 get할때 같이 넣어줘서 전달한다.	-> 트랜잭션 관리한다.
    												// read하기 이전부터 트랜잭션이 걸리며, 중간 processor 등 어디서 에러 발생시 rollback 된다.

    @Bean
    public FlatFileItemReader<Person> reader() {	// FlatFileItemReader 파일을 읽을 수 있는 클래스
      return new FlatFileItemReaderBuilder<Person>()
           .name("personItemReader")
           .resource(new ClassPathResource("csv/sample-data.csv")) // 지정한 파일에서 데이터를 읽어올 수 있도록 resource 설정.
           .delimited() 										   // 한 라인에서 각각의 컬럼을 어떤 구분자로 구분, 기본은 ',' 이며 다른걸 원할 시 "|" 등으로 선언
           .names(new String[]{"firstName", "lastName"}) 		   // String 배열이나, 여러개의 String 파라미터를 컬럼 순서대로 넘겨줄 수 있다. .names("firstName", "lastName")도 가능   
           .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
             setTargetType(Person.class); 						   // fieldSetMapper Mapper 역할로, 해당 클래스로 변환해준다.
           }})
           .build();
    }

    @Bean
    public PersonItemProcessor processor() {		// 비지니스 처리 담당, 항목이 유효하지 않다고 판단되는 경우 null 반환 -> Optinal 사용 필요없다.
       return new PersonItemProcessor();			
    }												

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {	// writer 부분 구현
        return new JdbcBatchItemWriterBuilder<Person>()
          .itemSqlParameterSourceProvider
          (new BeanPropertyItemSqlParameterSourceProvider<>())
          .sql("INSERT INTO people (person_id, first_name, last_name)  VALUES (people_seq.NEXTVAL, :firstName, :lastName)")
          .dataSource(dataSource)
          .build();
    }
    
    @Bean
    public Step step1(JdbcBatchItemWriter<Person> writer) {		// step 이란 job을 구성하는 하나 단계, 실제로 배치 실행되는 처리 정의 및 컨트롤 
       return stepBuilderFactory.get("step1")
          .<Person, Person> chunk(10)		// chunk 단위로 데이터 처리, chunk 단위로 Reader에 맞춘 후 chunk 단위만큼 다 차면 Processor를 하나씩 process한다. chunk단위 만큼 다 처리하면 writer에서 한번에 처리한다.
          .reader(reader())
          .processor(processor())
          .writer(writer)
          .build();
   }
    
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
         
         return jobBuilderFactory.get("importUserJob") // get메소드로 JobBuilder를 생성
           .incrementer(new RunIdIncrementer())
           .listener(listener)
           .flow(step1)
           .end()
           .build();
    }
}
