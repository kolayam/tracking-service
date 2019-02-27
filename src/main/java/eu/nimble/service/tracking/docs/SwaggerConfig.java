package eu.nimble.service.tracking.docs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.models.Contact;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {                                    
	@Bean
	public Docket simpleDiffServiceApi() {
	  return new Docket(DocumentationType.SWAGGER_2)
	  .groupName("calculator")
	  .apiInfo(apiInfo())
	  .select()
	  .apis(RequestHandlerSelectors.any())
	  .paths(PathSelectors.any())
	  .build()
	  .pathMapping("/");
	 
	}
	
	private ApiInfo apiInfo() {
		  return new ApiInfoBuilder()
		  .title("A simple calculator service")
		  .description("A simple calculator REST service made with Spring Boot in Java")
		  .version("1.0")
		  .build();
		}
}
