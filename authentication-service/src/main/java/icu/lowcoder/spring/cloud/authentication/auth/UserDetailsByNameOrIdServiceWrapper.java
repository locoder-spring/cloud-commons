package icu.lowcoder.spring.cloud.authentication.auth;

import icu.lowcoder.spring.commons.security.AccountModel;
import icu.lowcoder.spring.cloud.authentication.service.UserDetailsByIdService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class UserDetailsByNameOrIdServiceWrapper<T extends Authentication> implements
        AuthenticationUserDetailsService<T>, InitializingBean {

    private UserDetailsByIdService userDetailsByIdService;
    private UserDetailsService userDetailsService;

    public UserDetailsByNameOrIdServiceWrapper(UserDetailsByIdService userDetailsByIdService, UserDetailsService userDetailsService) {
        this.userDetailsByIdService = userDetailsByIdService;
        this.userDetailsService = userDetailsService;
    }

    public void setUserDetailsByIdService(UserDetailsByIdService userDetailsByIdService) {
        this.userDetailsByIdService = userDetailsByIdService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.userDetailsService, "UserDetailsService must be set");
        Assert.notNull(this.userDetailsByIdService, "UserDetailsByIdService must be set");
    }

    @Override
    public UserDetails loadUserDetails(T authentication) throws UsernameNotFoundException {
        Object authenticationToken = authentication.getPrincipal();
        if (authenticationToken instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) authenticationToken;
            Object principal = token.getPrincipal();
            if (principal instanceof AccountModel) {
                AccountModel accountModel = (AccountModel) principal;
                return this.userDetailsByIdService.loadUserById(accountModel.getId());
            }
        }

        if (!StringUtils.isEmpty(authentication.getName())) {
            return this.userDetailsService.loadUserByUsername(authentication.getName());
        }

        throw new UsernameNotFoundException("Can not load user details.");
    }
}
