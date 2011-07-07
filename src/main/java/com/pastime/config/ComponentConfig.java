package com.pastime.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration for application @Components such as @Services, @Repositories, and @Controllers.
 * Loads externalized property values required to configure the various application properties.
 * Not much else here, as we rely on @Component scanning in conjunction with @Inject by-type autowiring.
 * @author Keith Donald
 */
@Configuration
@ComponentScan(basePackages="com.pastime", excludeFilters={ @Filter(Configuration.class)} )
public class ComponentConfig {

	/**
	 * Properties to support the 'embedded' mode of operation.
	 */
	@Configuration
	@Profile("embedded")
	@PropertySource("classpath:com/pastime/config/embedded.properties")
	static class Embedded {
	}

	/**
	 * Properties to support the 'standard' mode of operation.
	 */
	@Configuration
	@Profile("standard")
	@PropertySource("classpath:application.properties")
	static class Standard {
	}

}