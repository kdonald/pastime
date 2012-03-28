package org.springframework.jdbc.core;

import java.io.IOException;
import java.io.LineNumberReader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.StringUtils;

public class SqlUtils {

    private static final String COMMENT_PREFIX = "--";

    public static String sql(Resource resource) {
        return sql(new EncodedResource(resource));
    }

    public static String sql(EncodedResource resource) {
        LineNumberReader reader = null;
        try {
            reader = new LineNumberReader(resource.getReader());
            String currentStatement = reader.readLine();
            StringBuilder scriptBuilder = new StringBuilder();
            while (currentStatement != null) {
                if (StringUtils.hasText(currentStatement)
                        && (COMMENT_PREFIX != null && !currentStatement.startsWith(COMMENT_PREFIX))) {
                    if (scriptBuilder.length() > 0) {
                        scriptBuilder.append('\n');
                    }
                    scriptBuilder.append(currentStatement);
                }
                currentStatement = reader.readLine();
            }
            return scriptBuilder.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read SQL resource", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {}
            }
        }
    }
}
