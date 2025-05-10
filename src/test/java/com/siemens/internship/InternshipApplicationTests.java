package com.siemens.internship;

import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.validator.EmailValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InternshipApplicationTests {

	@Test
	void contextLoads() {
	}
	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private EmailValidator emailValidator;

	@Test
	void testEmailValidatorInContext() {
		assertNotNull(emailValidator);
	}

	@Test
	void testRepositoryConnection() {
		assertDoesNotThrow(() -> itemRepository.count());
	}

	@ParameterizedTest
	@ValueSource(strings = {"valid@example.com", "user.name-tag@domain.co.uk"})
	void testValidEmails(String email) {
		assertTrue(emailValidator.validateEmail(email));
	}

	@ParameterizedTest
	@ValueSource(strings = {"invalid", "missing@domain"})
	void testInvalidEmails(String email) {
		assertFalse(emailValidator.validateEmail(email));
	}

}
