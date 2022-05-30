package icu.lowcoder.spring.cloud.organization.controller;

import icu.lowcoder.spring.commons.jpa.CommonEntity;
import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.organization.dao.AuthorityRepository;
import icu.lowcoder.spring.cloud.organization.dao.PositionRepository;
import icu.lowcoder.spring.cloud.organization.dto.request.AddPositionRequest;
import icu.lowcoder.spring.cloud.organization.dto.request.EditPositionRequest;
import icu.lowcoder.spring.cloud.organization.dto.response.PositionResponse;
import icu.lowcoder.spring.cloud.organization.dto.response.UUIDIdResponse;
import icu.lowcoder.spring.cloud.organization.entity.Authority;
import icu.lowcoder.spring.cloud.organization.entity.Position;
import icu.lowcoder.spring.cloud.organization.service.PositionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class PositionsController implements PositionsService {

    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_positions_add') or hasRole('SYSTEM_MANAGER')")
    public UUIDIdResponse add(@Valid @RequestBody AddPositionRequest request) {
        // code to upper case
        request.setCode(request.getCode().toUpperCase());

        // CODE 存在判断
        if (positionRepository.existsByCode(request.getCode())) {
            throw new HttpClientErrorException(HttpStatus.PRECONDITION_FAILED, request.getCode() + " 已存在");
        }

        Position position = BeanUtils.instantiate(Position.class, request, "authorities");
        // 权限处理
        List<Authority> requestAuthorities = new ArrayList<>();
        if (request.getAuthorities() != null && !request.getAuthorities().isEmpty()) {
            requestAuthorities.addAll(authorityRepository.findAllById(request.getAuthorities()));
        }
        if (!requestAuthorities.isEmpty()) {
            position.getAuthorities().addAll(requestAuthorities);
        }

        positionRepository.save(position);

        return new UUIDIdResponse(position.getId());
    }

    @Override
    @PreAuthorize("hasAuthority('org_positions_list') or hasRole('SYSTEM_MANAGER')")
    public Page<PositionResponse> page(@PageableDefault(sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return positionRepository.findAll(pageable)
                .map(position -> {
                    PositionResponse positionModel = BeanUtils.instantiate(PositionResponse.class, position, "authorities");
                    positionModel.setAuthorities(position.getAuthorities().stream().map(CommonEntity::getId).collect(Collectors.toList()));
                    return positionModel;
                });
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_positions_edit') or hasRole('SYSTEM_MANAGER')")
    public void edit(@PathVariable UUID id,  @Valid @RequestBody EditPositionRequest request) {
        Position position = getPosition(id);

        BeanUtils.copyProperties(request, position, "authorities");

        // 新的权限列表
        List<Authority> requestAuthorities = new ArrayList<>();
        if (request.getAuthorities() != null && !request.getAuthorities().isEmpty()) {
            requestAuthorities.addAll(authorityRepository.findAllById(request.getAuthorities()));
        }
        position.getAuthorities().clear();
        position.getAuthorities().addAll(requestAuthorities);
    }

    @Override
    @PreAuthorize("hasAuthority('org_positions_get') or hasRole('SYSTEM_MANAGER')")
    public PositionResponse get(@PathVariable UUID id) {
        Position position = getPosition(id);

        PositionResponse positionResponse = BeanUtils.instantiate(PositionResponse.class, position, "authorities");
        positionResponse.setAuthorities(position.getAuthorities().stream().map(CommonEntity::getId).collect(Collectors.toList()));
        return positionResponse;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('org_positions_del') or hasRole('SYSTEM_MANAGER')")
    public void del(@PathVariable UUID id) {
        Position position = getPosition(id);
        if (position.getBuiltIn()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "内置职位无法删除");
        }

        positionRepository.delete(position);
    }

    private Position getPosition(UUID id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "职位不存在"));
    }

}
