package org.springframework.templating;

public interface TemplateLoader {
    
    Template getTemplate(String location);
    
}
