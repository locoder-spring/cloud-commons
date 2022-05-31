package icu.lowcoder.spring.cloud.bridge.wechat;

import icu.lowcoder.spring.cloud.bridge.wechat.config.WeChatBridgeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@AutoConfigurationPackage
@EnableConfigurationProperties(WeChatBridgeProperties.class)
public class WeChatBridgeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeChatBridgeServiceApplication.class, args);
	}

}
