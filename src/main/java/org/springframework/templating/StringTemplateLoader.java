package org.springframework.templating;

public interface StringTemplateLoader {
    
    StringTemplate getTemplate(String location);
    
}
