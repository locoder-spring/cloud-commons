package icu.lowcoder.spring.cloud.config.db.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration(proxyBeanMethods = false)
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/management/**").access("hasAnyRole('SYSTEM_MANAGER', 'CONFIG_MANAGER')")
                .anyRequest().authenticated()
            .and().httpBasic()
            .and().formLogin()
            .and().csrf().disable();
    }

}
