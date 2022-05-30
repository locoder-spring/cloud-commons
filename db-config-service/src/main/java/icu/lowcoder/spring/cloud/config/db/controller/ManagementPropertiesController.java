package icu.lowcoder.spring.cloud.config.db.controller;

import icu.lowcoder.spring.cloud.config.db.dao.PropertiesRepository;
import icu.lowcoder.spring.cloud.config.db.dto.*;
import icu.lowcoder.spring.cloud.config.db.entity.Properties;
import icu.lowcoder.spring.cloud.config.db.service.ManagementPropertiesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.UUID;

@RestController
public class ManagementPropertiesController implements ManagementPropertiesService {

    @Autowired
    private PropertiesRepository propertiesRepository;

    @Override
    public Page<PropertyResponse> page(@Valid ListPropertiesParams params,
                                       @PageableDefault(sort = {"application", "profile", "key"}) Pageable pageable) {

        return propertiesRepository.findAll(
                Specification
                        .<Properties>where((root, cq, cb) -> {
                            if (StringUtils.hasText(params.getApplicationKeyword())) {
                                String v = "%" + params.getApplicationKeyword().trim() + "%";
                                return cb.like(root.get("application"), v);
                            }
                            return null;
                        })
                        .and((root, cq, cb) -> {
                            if (StringUtils.hasText(params.getProfile())) {
                                return cb.equal(root.get("profile"), params.getProfile().trim());
                            }
                            return null;
                        })
                        .and((root, cq, cb) -> {
                            if (StringUtils.hasText(params.getLabel())) {
                                return cb.equal(root.get("label"), params.getLabel().trim());
                            }
                            return null;
                        })
                        .and((root, cq, cb) -> {
                            if (StringUtils.hasText(params.getKeyKeyword())) {
                                String v = "%" + params.getKeyKeyword().trim() + "%";
                                return cb.like(root.get("key"), v);
                            }
                            return null;
                        })
                        .and((root, cq, cb) -> {
                            if (StringUtils.hasText(params.getValueKeyword())) {
                                String v = "%" + params.getValueKeyword().trim() + "%";
                                return cb.like(root.get("value"), v);
                            }
                            return null;
                        })
                , pageable)
                .map(property -> {
                    PropertyResponse response = new PropertyResponse();
                    BeanUtils.copyProperties(property, response);
                    return response;
                });
    }

    @Override
    @Transactional
    public void del(@PathVariable UUID propertyId) {
        Properties property = queryProperty(propertyId);

        propertiesRepository.delete(property);
    }

    @Override
    @Transactional
    public UUIDIdResponse add(@Valid @RequestBody AddPropertyRequest request) {
        Properties property = new Properties();
        BeanUtils.copyProperties(request, property);

        if (propertiesRepository.exists(Example.of(property))) {
            throw new HttpClientErrorException(HttpStatus.CONFLICT, "已存在相同配置属性");
        }

        propertiesRepository.save(property);

        return new UUIDIdResponse(property.getId());
    }

    @Override
    @Transactional
    public void edit(@PathVariable UUID propertyId, @Valid @RequestBody  EditPropertyRequest request) {
        Properties property = queryProperty(propertyId);
        if (!StringUtils.hasText(request.getValue())) {
            request.setValue(null);
        }
        BeanUtils.copyProperties(request, property);
    }

    private Properties queryProperty(UUID id) {
        return propertiesRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "配置属性不存在"));
    }

}
