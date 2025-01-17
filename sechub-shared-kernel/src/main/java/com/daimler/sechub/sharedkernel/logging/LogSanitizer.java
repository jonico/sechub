package com.daimler.sechub.sharedkernel.logging;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class LogSanitizer {

	private static final Pattern FORGERY_PATTERN = Pattern.compile("[\t\n\r]");

	public String sanitize(Object maybeContaminated, int maxLength) {
		String objAsString = null;
		if (maybeContaminated!=null) {
			objAsString = maybeContaminated.toString();
		}
		return sanitize(objAsString, maxLength);
	}

	/**
	 * Returns sanitized text.
	 *
	 * @param maybeContaminated
	 * @param maxLength         when >0 the returned string has this maximum
	 *                          length. use <=0 when Length doesn't matter
	 * @return sanitized text . Every character not being allowed will be replaced by <code>§</code> character
	 */
	public String sanitize(String maybeContaminated, int maxLength) {
		if (maybeContaminated == null) {
			return null;
		}
		String result = maybeContaminated;
		if (maxLength > 0 && result.length() > maxLength) {
			result = result.substring(0, maxLength);
		}
		result = FORGERY_PATTERN.matcher(result).replaceAll("§");
		return result;
	}

}
