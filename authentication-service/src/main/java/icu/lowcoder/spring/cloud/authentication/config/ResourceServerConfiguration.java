package icu.lowcoder.spring.cloud.authentication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration(proxyBeanMethods = false)
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.requestMatchers(requests -> {
            requests.antMatchers("/accounts/**")
                    .antMatchers("/oauth/sms-authorize")
                    .antMatchers("/management/**");
        }).authorizeRequests()
                .mvcMatchers(HttpMethod.POST, "/accounts").permitAll()
                .antMatchers("/oauth/sms-authorize").permitAll()
                .antMatchers("/management/**").access("hasAnyRole('SYSTEM_MANAGER', 'AUTH_MANAGER')")
                .anyRequest().authenticated()
        .and().httpBasic()
        .and().formLogin()
        .and().csrf().disable();
    }

}
