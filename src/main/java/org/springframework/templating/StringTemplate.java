package org.springframework.templating;

import java.util.Map;

public interface StringTemplate {

    String render(Map<String, Object> model);

}