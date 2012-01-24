package org.springframework.templating;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.springframework.core.io.ResourceLoader;

public class JMustacheStringTemplateLoader implements StringTemplateLoader {

    private final com.samskivert.mustache.Mustache.TemplateLoader templateLoader;

    private final com.samskivert.mustache.Mustache.Compiler compiler;

    private String prefix = "/WEB-INF/views/";
    
    private String suffix = ".html";
    
    public JMustacheStringTemplateLoader(ResourceLoader resourceLoader) {
        templateLoader = new ResourceTemplateLoader(resourceLoader);
        compiler = com.samskivert.mustache.Mustache.compiler().nullValue("").escapeHTML(false).withLoader(templateLoader);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    public StringTemplate getTemplate(String location) {
        Reader source = null;
        try {
           source = templateLoader.getTemplate(prefix + location + suffix);
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
    
    private static class JMustacheStringTemplate implements StringTemplate {

        private com.samskivert.mustache.Template template;
        
        public JMustacheStringTemplate(com.samskivert.mustache.Template template) {
            this.template = template;
        }

        public String render(Map<String, Object> model) {
            return template.execute(model);
        }
        
    }

}