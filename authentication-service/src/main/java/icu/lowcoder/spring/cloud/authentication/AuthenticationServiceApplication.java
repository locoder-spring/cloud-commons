package icu.lowcoder.spring.cloud.authentication;

import icu.lowcoder.spring.cloud.authentication.config.AuthProperties;
import icu.lowcoder.spring.cloud.authentication.config.CustomizedGrantTypesAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableDiscoveryClient
@SpringBootApplication
@ImportAutoConfiguration(CustomizedGrantTypesAutoConfiguration.class)
@AutoConfigurationPackage
@EnableConfigurationProperties(AuthProperties.class)
@EnableTransactionManagement
public class AuthenticationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationServiceApplication.class, args);
	}

}
