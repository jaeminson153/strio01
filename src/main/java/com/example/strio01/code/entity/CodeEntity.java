package com.example.strio01.code.entity;

import java.sql.Date;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Setter
@Getter
@Entity
@Table(name = "code_master")
public class CodeEntity {
	@Id
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "board_seq_generator")
	//@SequenceGenerator(name = "board_seq_generator", sequenceName = "board_num_seq", allocationSize = 1)
	private String  codeGroup, codeId, codeName, codeDesc, useYn;
	private Date createdAt, updatedAt;	
}
