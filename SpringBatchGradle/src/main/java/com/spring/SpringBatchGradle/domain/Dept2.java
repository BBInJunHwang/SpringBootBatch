package com.spring.SpringBatchGradle.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Dept2 {

	@Id
	Integer deptNo;
	String dName;
	String loc;
}
