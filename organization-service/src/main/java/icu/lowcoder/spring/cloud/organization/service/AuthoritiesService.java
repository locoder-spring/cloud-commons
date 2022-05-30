package icu.lowcoder.spring.cloud.organization.service;

import icu.lowcoder.spring.cloud.organization.dto.request.AddAuthorityRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.EditAuthorityRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.ListAuthoritiesParams;
import icu.lowcoder.spring.cloud.organization.dto.response.AuthorityResponse;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/authorities")
public interface AuthoritiesService {

    @PostMapping
    UUIDIdResponse add(AddAuthorityRequest request);

    @GetMapping
    Page<AuthorityResponse> page(ListAuthoritiesParams params, Pageable pageable);

    @PutMapping("/{id}")
    void edit(@PathVariable UUID id, EditAuthorityRequest request);

    @GetMapping("/{id}")
    AuthorityResponse get(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    void del(@PathVariable UUID id);

}
