package com.daimler.sechub.sharedkernel.configuration.login;

import static org.junit.Assert.*;

import javax.crypto.SealedObject;

import org.junit.Before;
import org.junit.Test;


public class AutoDetectUserLoginConfigurationTest {

	private AutoDetectUserLoginConfiguration config;

	@Before
	public void before() {
		/* prepare*/
		config = new AutoDetectUserLoginConfiguration();
	}

	@Test
	public void when_passwort_set_ensure_password_field_is_a_sealed_object() {

		/* execute */
		config.setPassword("a".toCharArray());

		/* test */
		assertTrue(config.password instanceof SealedObject);
	}

	@Test
	public void no_password_set_returns_null_as_password() {

		/* execute + test */
		assertNull(config.getPassword());
	}

	@Test
	public void encrypted_password_set_get_works() {

		/* execute */
		config.setPassword("abcdefgh$_ü".toCharArray());

		/* test */
		assertEquals("abcdefgh$_ü",new String(config.getPassword()));

	}

}
