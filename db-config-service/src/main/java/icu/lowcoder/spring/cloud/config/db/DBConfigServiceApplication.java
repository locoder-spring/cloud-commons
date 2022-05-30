package icu.lowcoder.spring.cloud.config.db;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableDiscoveryClient
@SpringBootApplication
@AutoConfigurationPackage
@EnableTransactionManagement
public class DBConfigServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DBConfigServiceApplication.class, args);
	}

}
