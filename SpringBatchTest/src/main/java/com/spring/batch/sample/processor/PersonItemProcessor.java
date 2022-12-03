package com.spring.batch.sample.processor;

import org.springframework.batch.item.ItemProcessor;

import com.spring.batch.sample.model.Person;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonItemProcessor implements ItemProcessor<Person,Person> {	// itemProcessor<I,O> -> I : itemReader에서 받을 데이터 타
																			//                    -> O : itemWriter로 보낼 데이터 타입
    @Override
    public Person process(Person item){		// 해당 로직은 process 부분 재정의, I 데이터를 받아서 이름을 대문자로 변환 후 O로 전달 
    										// 만약 필터기능 넣고 싶으면, 어떤 조건을 추가한 후 if(firstName != 'joe') return null; 로 null 리턴시 writer로 전달안됨 
        final String firstName = item.getFirstName().toUpperCase();
        final String lastName = item.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("Converting ({}) into ({})",item,transformedPerson);

        return transformedPerson;
    }
}