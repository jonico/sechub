// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class JSONConverter {

	private static final JSONConverter INSTANCE = new JSONConverter();

	/**
	 * @return shared instance
	 */
	public static JSONConverter get() {
		return INSTANCE;
	}

	private ObjectMapper mapper;

	public JSONConverter() {
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
		// reason: otherwise jackson does all single ones write as not being an array which comes up to problems agani
		mapper.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
		
		// http://www.baeldung.com/jackson-ignore-null-fields
		mapper.setSerializationInclusion(Include.NON_NULL);
		// http://www.baeldung.com/jackson-optional
		mapper.registerModule(new Jdk8Module());
	}

	public String toJSON(Object object) throws JSONConverterException {
		if (object == null) {
			return "null";
		}
		try {
			byte[] bytes = mapper.writeValueAsBytes(object);
			return new String(bytes);
		} catch (JsonProcessingException e) {
			throw new JSONConverterException("Was not able to convert " + object.getClass().getName() + " to JSON", e);
		}
	}

	public <T> T fromJSON(Class<T> clazz, String jSON) throws JSONConverterException {
		if (clazz == null) {
			throw new IllegalStateException("clazz may not be null!");
		}
		/* Fall back for null values to empty string - avoid NPE */
		String string = jSON;
		if (jSON == null) {
			string = "";
		}
		try {
			byte[] bytes = string.getBytes();
			return mapper.readValue(bytes, clazz);
		} catch (IOException e) {
			throw new JSONConverterException(
					"Was not able to convert JSON string to " + clazz + " object\nContent was:\n" + jSON, e);
		}
	}

}
