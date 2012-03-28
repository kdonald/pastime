package org.springframework.templating;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.ResourceLoader;

public class JMustacheStringTemplateLoader implements StringTemplateLoader {

    private final com.samskivert.mustache.Mustache.TemplateLoader templateLoader;

    private final com.samskivert.mustache.Mustache.Compiler compiler;

    private final ConcurrentHashMap<String, StringTemplate> cachedTemplates = new ConcurrentHashMap<String, StringTemplate>();
    
    private String prefix;
    
    private String suffix;
    
    private boolean cache = true;
    
    public JMustacheStringTemplateLoader(ResourceLoader resourceLoader) {
        templateLoader = new ResourceTemplateLoader(resourceLoader);
        compiler = com.samskivert.mustache.Mustache.compiler().nullValue("").escapeHTML(false).withLoader(templateLoader);
        setPrefix("/WEB-INF/views/");
        setSuffix(".html");
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    public void setCache(boolean cache) {
        this.cache = cache;
    }
    
    public StringTemplate getTemplate(String location) {
        location = prefix + location + suffix;
        StringTemplate template = cachedTemplates.get(location);
        if (template != null) {
            return template;
        }
        return cacheTemplate(location);
    }

    private StringTemplate cacheTemplate(String location) {
        StringTemplate template = loadTemplate(location);
        if (cache) {
            cachedTemplates.put(location, template);
        }
        return template;        
    }
    
    private StringTemplate loadTemplate(String location) {
        Reader source = null;
        try {
           source = templateLoader.getTemplate(location);
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