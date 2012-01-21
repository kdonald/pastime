package org.springframework.templating;

import java.util.Map;

public interface Template {

    String render(Map<String, Object> model);

}