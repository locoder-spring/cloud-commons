package icu.lowcoder.spring.cloud.authentication.manager;

import icu.lowcoder.spring.commons.security.AccountModel;
import icu.lowcoder.spring.cloud.authentication.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountMergeManager {

    @Autowired
    @Qualifier("oauth2RedisTokenStore")
    private TokenStore tokenStore;

    @Autowired
    @Qualifier("oauth2ProviderManager")
    public ProviderManager authenticationManager;

    public void switchTo(Account targetAccount) {
        OAuth2Authentication authentication = getOAuth2Authentication();
        if (this.authenticationManager == null || authentication.isClientOnly()) {
            throw new InternalAuthenticationServiceException("Switch is not allowed.");
        }
        OAuth2AccessToken oAuth2AccessToken = tokenStore.getAccessToken(authentication);
        if (oAuth2AccessToken == null) {
            throw new InternalAuthenticationServiceException("Can't retrieve an access token.");
        }

        Object principal = authentication.getUserAuthentication().getPrincipal();
        if (principal instanceof AccountModel) {
            log.debug("switch account, token: {}, [id:{}, name:{}] switch to [id:{}, name:{}]",
                    oAuth2AccessToken.getValue(),
                    ((AccountModel) principal).getId(), ((AccountModel) principal).getName(),
                    targetAccount.getId(), targetAccount.getName()
            );

            ((AccountModel) principal).setId(targetAccount.getId());
            ((AccountModel) principal).setPhone(targetAccount.getPhone());
        }

        Authentication user = new PreAuthenticatedAuthenticationToken(authentication.getUserAuthentication(),
                authentication.getUserAuthentication().getCredentials(),
                authentication.getAuthorities()
        );
        user = authenticationManager.authenticate(user);
        Object details = authentication.getDetails();
        authentication = new OAuth2Authentication(authentication.getOAuth2Request(), user);
        authentication.setDetails(details);

        tokenStore.storeAccessToken(oAuth2AccessToken, authentication);
        tokenStore.storeRefreshToken(oAuth2AccessToken.getRefreshToken(), authentication);
    }

    private OAuth2Authentication getOAuth2Authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            return (OAuth2Authentication) authentication;
        } else {
            throw new InternalAuthenticationServiceException("Can't get OAuth2Authentication.");
        }
    }
}
