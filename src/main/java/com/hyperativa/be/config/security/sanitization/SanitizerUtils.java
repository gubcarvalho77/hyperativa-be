package com.hyperativa.be.config.security.sanitization;

import lombok.experimental.UtilityClass;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

@UtilityClass
public class SanitizerUtils {

    private static final Pattern MALICIOUS_PATTERN =
            Pattern.compile("(?i)<script|onerror|onload|alert\\(", Pattern.CASE_INSENSITIVE);

//    public static boolean isMalicious(String input) {
//        return input != null && MALICIOUS_PATTERN.matcher(input).find();
//    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        var clean = input.replaceAll("<[^>]+>", "")
                .replaceAll("(?i)on\\w+\\s*=\\s*['\"].*?['\"]", "")
                .replaceAll("(?i)javascript:", "")
                .trim();

        return HtmlUtils.htmlEscape(clean);
    }
}
