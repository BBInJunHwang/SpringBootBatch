package com.spring.batch.sample.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.spring.batch.sample.model.Person;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	   private final JdbcTemplate jdbcTemplate;

	   @Override
	   public void afterJob(JobExecution jobExecution) {

	        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
	             log.info("### JOB 끝남 ####");
	                
	             jdbcTemplate.query("SELECT first_name, last_name FROM people",
	                (rs, row) -> new Person(
	                  rs.getString(1),
	                  rs.getString(2))
	               ).forEach(person -> log.info(" <" + person + "> 디비에 등록됨")); 
	         }
	   }
	}