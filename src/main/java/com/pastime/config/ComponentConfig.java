package com.pastime.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for application @Components such as @Services, @Repositories, and @Controllers.
 * Not much else here, as we rely on @Component scanning in conjunction with @Inject by-type autowiring.
 * @author Keith Donald
 */
@Configuration
@ComponentScan(basePackages="com.pastime", excludeFilters={ @Filter(Configuration.class)} )
public class ComponentConfig {

}