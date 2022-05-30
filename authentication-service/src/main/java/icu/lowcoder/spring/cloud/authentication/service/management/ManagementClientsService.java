package icu.lowcoder.spring.cloud.authentication.service.management;

import icu.lowcoder.spring.cloud.authentication.dto.SaveClientsRequest;
import icu.lowcoder.spring.cloud.authentication.dto.ClientsResponse;
import icu.lowcoder.spring.cloud.authentication.dto.KeywordParams;
import icu.lowcoder.spring.cloud.authentication.dto.UUIDIdResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/management/clients")
public interface ManagementClientsService {

    @GetMapping
    Page<ClientsResponse> page(KeywordParams params, Pageable pageable);

    @PostMapping
    UUIDIdResponse add(SaveClientsRequest request);

    @PutMapping("/{clientId}")
    void edit(@PathVariable UUID clientId, SaveClientsRequest request);

    @DeleteMapping("/{clientId}")
    void del(@PathVariable UUID clientId);
}
