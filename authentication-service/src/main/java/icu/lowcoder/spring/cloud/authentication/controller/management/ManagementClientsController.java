package icu.lowcoder.spring.cloud.authentication.controller.management;

import icu.lowcoder.spring.cloud.authentication.dao.AuthClientRepository;
import icu.lowcoder.spring.cloud.authentication.dto.ClientsResponse;
import icu.lowcoder.spring.cloud.authentication.dto.KeywordParams;
import icu.lowcoder.spring.cloud.authentication.dto.SaveClientsRequest;
import icu.lowcoder.spring.cloud.authentication.dto.UUIDIdResponse;
import icu.lowcoder.spring.cloud.authentication.entity.AuthClient;
import icu.lowcoder.spring.cloud.authentication.service.management.ManagementClientsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

@RestController
public class ManagementClientsController implements ManagementClientsService {
    @Autowired
    private AuthClientRepository clientRepository;

    @Override
    public Page<ClientsResponse> page(
            @Valid KeywordParams params,
            @PageableDefault(sort = {"clientId", "id"}) Pageable pageable) {
        return clientRepository.findAll((root, cq, cb) -> {
            if (StringUtils.hasText(params.getKeyword())) {
                String v = "%" + params.getKeyword().trim() + "%";
                return cb.or(
                        cb.like(root.get("clientId"), v),
                        cb.like(root.get("clientName"), v)
                );
            }
            return null;
        }, pageable).map(client -> {
            ClientsResponse response = new ClientsResponse();
            BeanUtils.copyProperties(client, response, "authorities", "grantTypes");
            if (StringUtils.hasText(client.getAuthorities())) {
                response.setAuthorities(new HashSet<>(Arrays.asList(client.getAuthorities().split(","))));
            }
            if (StringUtils.hasText(client.getGrantTypes())) {
                response.setGrantTypes(new HashSet<>(Arrays.asList(client.getGrantTypes().split(","))));
            }
            return response;
        });
    }

    @Override
    @Transactional
    public UUIDIdResponse add(@Valid @RequestBody SaveClientsRequest request) {
        if (clientRepository.existsByClientId(request.getClientId())) {
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "已存在该客户端");
        }

        AuthClient authClient = new AuthClient();
        BeanUtils.copyProperties(request, authClient, "authorities", "greatTypes");

        if (!request.getAuthorities().isEmpty()) {
            authClient.setAuthorities(StringUtils.collectionToCommaDelimitedString(request.getAuthorities()));
        }
        if (!request.getGrantTypes().isEmpty()) {
            authClient.setGrantTypes(StringUtils.collectionToCommaDelimitedString(request.getGrantTypes()));
        }

        clientRepository.save(authClient);

        return new UUIDIdResponse(authClient.getId());
    }

    @Override
    @Transactional
    public void edit(@PathVariable UUID clientId, @Valid @RequestBody SaveClientsRequest request) {
        AuthClient client = queryClient(clientId);

        BeanUtils.copyProperties(request, client, "authorities", "greatTypes");

        if (!request.getAuthorities().isEmpty()) {
            client.setAuthorities(StringUtils.collectionToCommaDelimitedString(request.getAuthorities()));
        } else {
            client.setAuthorities(null);
        }
        if (!request.getGrantTypes().isEmpty()) {
            client.setGrantTypes(StringUtils.collectionToCommaDelimitedString(request.getGrantTypes()));
        } else {
            client.setGrantTypes(null);
        }
    }

    @Override
    @Transactional
    public void del(@PathVariable UUID clientId) {
        AuthClient client = queryClient(clientId);

        clientRepository.delete(client);
    }


    private AuthClient queryClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "客户端不存在"));
    }
}
