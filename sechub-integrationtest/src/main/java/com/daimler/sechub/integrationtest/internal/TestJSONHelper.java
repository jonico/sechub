// SPDX-License-Identifier: MIT
package com.daimler.sechub.integrationtest.internal;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class TestJSONHelper {

	private static final TestJSONHelper INSTANCE = new TestJSONHelper();

	/**
	 * @return shared instance
	 */
	public static TestJSONHelper get() {
		return INSTANCE;
	}

	private ObjectMapper mapper;

	public TestJSONHelper() {
		// https://github.com/FasterXML/jackson-core/wiki/JsonParser-Features
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
		jsonFactory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

		mapper = new ObjectMapper(jsonFactory);
		/*
		 * next line will write single element array as simple strings. There was an
		 * issue with this when serializing/deserializing SimpleMailMessage class from
		 * spring when only one "to" defined but was an array - jackson had problems see
		 * also: https://github.com/FasterXML/jackson-databind/issues/720 and
		 * https://stackoverflow.com/questions/39041496/how-to-enforce-accept-single-
		 * value-as-array-in-jacksons-deserialization-process
		 */
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

		// but we do NOT use SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED !
		// reason: otherwise jackson does all single ones write as not being an array
		// which comes up to problems agani
		mapper.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);

		// http://www.baeldung.com/jackson-ignore-null-fields
		mapper.setSerializationInclusion(Include.NON_NULL);
		// http://www.baeldung.com/jackson-optional
		mapper.registerModule(new Jdk8Module());
	}

	public void assertValidJson(String string) {
		try {
			readTree(string);
		} catch (IllegalStateException e) {
			throw new IllegalStateException("This is not valid json:\n" + string, e);
		}
	}

	public JsonNode readTree(String string) {
		JsonNode node;
		try {
			node = mapper.reader().readTree(string);
		} catch (IOException e) {
			throw new IllegalStateException("Did not expect IO problems", e);
		}
		return node;
	}


}
