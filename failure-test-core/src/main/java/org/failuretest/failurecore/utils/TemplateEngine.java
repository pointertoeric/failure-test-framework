package org.failuretest.failurecore.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TemplateEngine {
    private static final Logger LOG = LoggerFactory.getLogger(TemplateEngine.class);

    private TemplateEngine() {}

    private static final Jinjava jinjava = new Jinjava();

    private static String escapseString(String input) {
        String[] lines = input.split("\n");
        StringBuilder result = new StringBuilder();
        result.append("\"");
        for(String line : lines) {
            if (line.trim().equals("")) {
                continue;
            }
            String escaped = line.replace("`", "\\`")
                    .replace("\"", "\\\"")
                    .replace("$", "\\$");
            result.append(escaped + "\\n");
        }
        result.append("\"");
        return result.toString();
    }

    public static String loadTemplate(String template, Map<String, Object> data) {
        String renderedTemplate = "";
        try {
            String templateStr = Resources.toString(Resources.getResource(template), Charsets.UTF_8);
            renderedTemplate = jinjava.render(templateStr, data);

        } catch (Exception e) {
            LOG.error("template error", e);
            throw new IllegalStateException(e.getMessage());
        }
        return escapseString(renderedTemplate);
   }
}
