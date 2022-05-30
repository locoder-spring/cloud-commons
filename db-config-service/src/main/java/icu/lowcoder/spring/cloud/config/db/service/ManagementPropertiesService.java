package icu.lowcoder.spring.cloud.config.db.service;

import icu.lowcoder.spring.cloud.config.db.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/management/properties")
public interface ManagementPropertiesService {

    @GetMapping
    Page<PropertyResponse> page(ListPropertiesParams params, Pageable pageable);

    @DeleteMapping("/{propertyId}")
    void del(@PathVariable UUID propertyId);

    @PostMapping
    UUIDIdResponse add(AddPropertyRequest request);

    @PutMapping("/{propertyId}")
    void edit(@PathVariable UUID propertyId, EditPropertyRequest request);


}
