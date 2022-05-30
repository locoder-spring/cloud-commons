package icu.lowcoder.spring.cloud.organization.service;

import icu.lowcoder.spring.cloud.organization.dto.request.AddPositionRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.EditPositionRequest;
import icu.lowcoder.spring.cloud.organization.dto.response.PositionResponse;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/positions")
public interface PositionsService {

    @PostMapping
    UUIDIdResponse add(AddPositionRequest request);

    @GetMapping
    Page<PositionResponse> page(Pageable pageable);

    @PutMapping("/{id}")
    void edit(@PathVariable UUID id, EditPositionRequest request);

    @GetMapping("/{id}")
    PositionResponse get(@PathVariable UUID id);

    @DeleteMapping("/{id}")
    void del(@PathVariable UUID id);

}
