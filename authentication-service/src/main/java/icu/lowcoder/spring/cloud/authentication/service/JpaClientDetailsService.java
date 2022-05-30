package icu.lowcoder.spring.cloud.authentication.service;

import icu.lowcoder.spring.cloud.authentication.dao.AuthClientRepository;
import icu.lowcoder.spring.cloud.authentication.entity.AuthClient;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

@Primary
@Service("oauth2ClientDetailsService")
public class JpaClientDetailsService implements ClientDetailsService {

    private final AuthClientRepository clientRepository;

    public JpaClientDetailsService(AuthClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        AuthClient client = clientRepository.findByClientId(clientId);
        if (client == null) {
            throw new NoSuchClientException("No client found with id = " + clientId);
        }

        return baseClientDetails(client);
    }

    private ClientDetails baseClientDetails(AuthClient client) {
        return new BaseClientDetails(client.getClientId(), null, client.getScopes(), client.getGrantTypes(), client.getAuthorities());
    }

}
