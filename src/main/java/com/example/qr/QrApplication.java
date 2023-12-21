	package com.example.qr;

	import org.springframework.boot.SpringApplication;
	import org.springframework.boot.autoconfigure.SpringBootApplication;
	import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
	import org.springframework.context.annotation.Bean;
	import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
	import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

	@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
	public class QrApplication {

		public static void main(String[] args) {
			SpringApplication.run(QrApplication.class, args);
		}
		@Bean
		public WebMvcConfigurer webMvcConfigurer() {
			return new WebMvcConfigurer() {
				@Override
				public void addResourceHandlers(ResourceHandlerRegistry registry) {
					registry.addResourceHandler("/generated-qr-codes/**").addResourceLocations("file:./generated-qr-codes/");
					registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
					registry.addResourceHandler("/uploaded-images/**").addResourceLocations("file:uploaded-images/");
				}
			};
		}

	}

