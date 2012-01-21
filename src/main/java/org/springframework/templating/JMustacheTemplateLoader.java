package org.springframework.templating;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.springframework.core.io.ResourceLoader;

public class JMustacheTemplateLoader implements TemplateLoader {

    private final com.samskivert.mustache.Mustache.TemplateLoader templateLoader;

    private final com.samskivert.mustache.Mustache.Compiler compiler;

    public JMustacheTemplateLoader(ResourceLoader resourceLoader) {
        templateLoader = new ResourceTemplateLoader(resourceLoader);
        compiler = com.samskivert.mustache.Mustache.compiler().nullValue("").withLoader(templateLoader);
    }

    public Template getTemplate(String location) {
        Reader source = null;
        try {
           source = templateLoader.getTemplate("/WEB-INF/views/" + location + ".html");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        com.samskivert.mustache.Template template = compiler.compile(source);
        return new JMustacheStringTemplate(template);        
    }

    private static class ResourceTemplateLoader implements com.samskivert.mustache.Mustache.TemplateLoader {

        private static final String DEFAULT_ENCODING = "UTF-8";

        private final ResourceLoader resourceLoader;

        private String encoding = DEFAULT_ENCODING;

        public ResourceTemplateLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public Reader getTemplate(String name) throws Exception {
            return new InputStreamReader(resourceLoader.getResource(name).getInputStream(), encoding);
        }

    }
    
    private static class JMustacheStringTemplate implements Template {

        private com.samskivert.mustache.Template template;
        
        public JMustacheStringTemplate(com.samskivert.mustache.Template template) {
            this.template = template;
        }

        public String render(Map<String, Object> model) {
            return template.execute(model);
        }
        
    }

}