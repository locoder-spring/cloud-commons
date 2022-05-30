package icu.lowcoder.spring.cloud.content.feign;


import icu.lowcoder.spring.commons.feign.ServiceAuthenticateFeignConfiguration;
import icu.lowcoder.spring.cloud.content.feign.model.FileAcl;
import icu.lowcoder.spring.cloud.content.feign.model.FileDescription;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(contextId = "CommonsFilesClient", value = "file-service", configuration = {ServiceAuthenticateFeignConfiguration.class})
public interface CommonsFilesClient {

    @PutMapping(value = "/files/{ids}")
    List<FileDescription> saveTmpFile(
            @PathVariable(value = "ids") UUID[] ids,
            @RequestParam(value = "toPath") String toPath,
            @RequestParam(value = "acl", required = false) FileAcl acl
    );

}
