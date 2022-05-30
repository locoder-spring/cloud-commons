package icu.lowcoder.spring.cloud.file.controller;

import icu.lowcoder.spring.cloud.file.dict.FileAcl;
import icu.lowcoder.spring.cloud.file.dto.FileDescription;
import icu.lowcoder.spring.cloud.file.dto.FileUploadRequest;
import icu.lowcoder.spring.cloud.file.dto.ObjectUUIDIdResponse;
import icu.lowcoder.spring.cloud.file.manager.FileNotFoundException;
import icu.lowcoder.spring.cloud.file.manager.FilesManager;
import icu.lowcoder.spring.cloud.file.service.FilesService;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

@RestController
public class FilesController implements FilesService {

    private final FilesManager filesManager;

    public FilesController(FilesManager filesManager) {
        this.filesManager = filesManager;
    }

    @Override
    @SneakyThrows
    @Transactional
    public ObjectUUIDIdResponse upload(@Valid FileUploadRequest request) {
        String name = String.format("%s.%s", UUID.randomUUID(), StringUtils.getFilenameExtension(request.getFile().getOriginalFilename()));

        FileDescription fileDescription = filesManager.upload(request.getFile(), null, name);

        return new ObjectUUIDIdResponse(fileDescription.getId());
    }

    /**
     * 不能由用户发起调用
     */
    @Override
    @PreAuthorize("#oauth2.client")
    @Transactional
    public List<FileDescription> transfer(
            @PathVariable UUID[] ids,
            @RequestParam String toPath,
            @RequestParam(value = "acl", required = false) FileAcl acl
    ) {
        return filesManager.transfer(ids, toPath, acl);
    }

    @Override
    public FileDescription getDesc(@PathVariable UUID id) {
        FileDescription fileDescription;
        try {
            fileDescription = filesManager.getDesc(id);
        } catch (FileNotFoundException e) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "文件不存在");
        }

        return fileDescription;
    }

    @Override
    @SneakyThrows
    public void getByte(@PathVariable UUID id, HttpServletResponse response) {
        FileDescription description;
        try {
            description = filesManager.getDesc(id);
        } catch (FileNotFoundException e) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "文件不存在");
        }

        @Cleanup InputStream inputStream =  filesManager.getInputStream(id);

        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(description.getName(),"UTF-8"));
        response.addHeader("Content-Length", description.getSize() + "");
        response.setContentType(description.getType());

        @Cleanup OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
        FileCopyUtils.copy(inputStream, outputStream);
    }
}
