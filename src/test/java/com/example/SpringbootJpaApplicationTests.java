package com.example;

import com.example.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SpringbootJpaApplicationTests {

	@Autowired
	private PersonRepository personRepository;

	//add test case for all endpoints in SpringbootJpaApplication.java
	@Test
	public void contextLoads() {
	}	
		
	@Test
	public void shouldReturnPersonWithNameLu() {
		//assert returned list contains person with name "John"
		assertEquals("lu", personRepository.findOneByName("lu").getName());
	}

	@Test
	public void testGetPersonByName() {
	}

	@Test
	public void testGetPersonsByAddress() {
	}

	@Test
	public void testGetCount() {
	}

	@Test
	public void testGetPersonByAge() {
	}
}




