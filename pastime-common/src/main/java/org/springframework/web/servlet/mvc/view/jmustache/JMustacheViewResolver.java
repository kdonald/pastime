package org.springframework.web.servlet.mvc.view.jmustache;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;
import com.samskivert.mustache.Template;

public class JMustacheViewResolver extends AbstractTemplateViewResolver implements ViewResolver {

    private final TemplateLoader templateLoader;
    
    private final Compiler compiler;
    
    public JMustacheViewResolver(ResourceLoader resourceLoader) {
        setViewClass(JMustacheView.class);
        setExposeSpringMacroHelpers(false);
        setPrefix("/WEB-INF/views/");
        setSuffix(".html");
        templateLoader = new ResourceTemplateLoader(resourceLoader);
        compiler = Mustache.compiler().nullValue("").withLoader(templateLoader);        
    }

    @Override
    protected Class<?> requiredViewClass() {
        return JMustacheView.class;
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        JMustacheView view = (JMustacheView) super.buildView(viewName);
        Template template = compiler.compile(templateLoader.getTemplate(view.getUrl()));
        view.setTemplate(template);
        return view;
    }

    private static class ResourceTemplateLoader implements TemplateLoader {

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
    
    private static class JMustacheView extends AbstractTemplateView {

        private Template template;

        public void setTemplate(Template template) {
            this.template = template;
        }

        @Override
        protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            response.setContentType(getContentType());
            final Writer writer = response.getWriter();
            try {
                template.execute(model, writer);
            } finally {
                writer.flush();
            }
        }

    }
    
}