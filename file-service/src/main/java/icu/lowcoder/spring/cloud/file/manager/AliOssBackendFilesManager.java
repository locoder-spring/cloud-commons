package icu.lowcoder.spring.cloud.file.manager;

import icu.lowcoder.spring.commons.ali.oss.AliOssClient;
import icu.lowcoder.spring.commons.security.SecurityUtils;
import icu.lowcoder.spring.commons.util.spring.BeanUtils;
import icu.lowcoder.spring.cloud.file.dao.FileUploadRecordRepository;
import icu.lowcoder.spring.cloud.file.dict.FileAcl;
import icu.lowcoder.spring.cloud.file.dto.FileDescription;
import icu.lowcoder.spring.cloud.file.entity.FileUploadRecord;
import io.vavr.collection.Stream;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AliOssBackendFilesManager implements FilesManager {
    private final FileUploadRecordRepository fileUploadRecordRepository;
    private final AliOssClient aliOssClient;

    public AliOssBackendFilesManager(FileUploadRecordRepository fileUploadRecordRepository,
                                     @Autowired(required = false) AliOssClient aliOssClient) {
        this.fileUploadRecordRepository = fileUploadRecordRepository;
        this.aliOssClient = aliOssClient;
    }

    @Override
    @SneakyThrows
    @Transactional
    public FileDescription upload(MultipartFile multipartFile, String path, String name) {
        if (path == null || StringUtils.isEmpty(path.trim())) {
            path = getAliOssClient().getTempDir();
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String objectName = String.format("%s/%s", path, name);

        @Cleanup InputStream inputStream = multipartFile.getInputStream();
        getAliOssClient().putObject(objectName, inputStream);

        // 本地记录
        FileUploadRecord newRecord = new FileUploadRecord();
        //newRecord.setId(UUID.randomUUID());
        newRecord.setPath(path);
        newRecord.setExtension(org.springframework.util.StringUtils.getFilenameExtension(multipartFile.getOriginalFilename()));
        newRecord.setName(name);
        newRecord.setOriginalFilename(multipartFile.getOriginalFilename());
        newRecord.setSize(multipartFile.getSize());
        newRecord.setType(multipartFile.getContentType());
        newRecord.setUserId(SecurityUtils.getPrincipalId());
        fileUploadRecordRepository.save(newRecord);

        // 返回
        FileDescription fd = new FileDescription();
        fd.setPath(objectName);
        fd.setId(newRecord.getId());
        fd.setSize(newRecord.getSize());
        fd.setName(newRecord.getOriginalFilename());

        return fd;
    }

    @Override
    public FileDescription getDesc(UUID id) {
        Optional<FileUploadRecord> uploadRecord = fileUploadRecordRepository.findById(id);
        uploadRecord.orElseThrow(FileNotFoundException::new);

        String objectName = String.format("%s/%s", uploadRecord.get().getPath(), uploadRecord.get().getName());
        if (getAliOssClient().exist(objectName)) {
            String contentType = getAliOssClient().getObject(objectName).getObjectMetadata().getContentType();

            return uploadRecord.map(ur -> {
                FileDescription fd = BeanUtils.instantiate(FileDescription.class, ur);
                fd.setType(contentType);
                return fd;
            }).get();
        } else {
            throw new FileNotFoundException("不能获取文件：" + objectName);
        }
    }

    @Override
    public InputStream getInputStream(UUID id) {
        Optional<FileUploadRecord> uploadRecord = fileUploadRecordRepository.findById(id);
        uploadRecord.orElseThrow(FileNotFoundException::new);

        String objectName = String.format("%s/%s", uploadRecord.get().getPath(), uploadRecord.get().getName());

        if (getAliOssClient().exist(objectName)) {
            return getAliOssClient().getObject(objectName).getObjectContent();
        } else {
            throw new FileNotFoundException("不能获取文件：" + objectName);
        }
    }

    @Override
    @Transactional
    public List<FileDescription> transfer(UUID[] ids, String path) {
        return transfer(ids, path, null);
    }

    @Override
    @Transactional
    public List<FileDescription> transfer(UUID[] ids, String path, FileAcl acl) {
        if (path != null && path.startsWith("/")) {
            path = path.replaceAll("/(.*)", "$1");
        }
        Iterable<FileUploadRecord> uploadRecords = fileUploadRecordRepository.findAllById(Arrays.asList(ids));

        String principalId = SecurityUtils.getPrincipalId();
        String finalPath = path;

        // 更新acl
        Map<String, String> aclHeader = null;
        if (acl != null) {
            aclHeader = new HashMap<>();
            aclHeader.put("x-oss-object-acl", acl.getAliOssAcl());
        }

        Map<String, String> finalAclHeader = aclHeader;
        List<FileUploadRecord> newRecords = Stream.ofAll(uploadRecords).map(ur -> {
            // oss 转存
            String sourceKey = String.format("%s/%s", ur.getPath(), ur.getName());
            String destinationKey = String.format("%s/%s", finalPath, ur.getName());
            getAliOssClient().copyObject(sourceKey, destinationKey, finalAclHeader);

            // 本地记录
            FileUploadRecord newRecord = new FileUploadRecord();
            newRecord.setPath(finalPath);
            newRecord.setExtension(ur.getExtension());
            newRecord.setName(ur.getName());
            newRecord.setOriginalFilename(ur.getOriginalFilename());
            newRecord.setSize(ur.getSize());
            newRecord.setType(ur.getType());
            newRecord.setUserId(principalId);

            return newRecord;
        }).collect(Collectors.toList());

        fileUploadRecordRepository.saveAll(newRecords);

        return newRecords.stream().map(ur -> {
            FileDescription fd = BeanUtils.instantiate(FileDescription.class, ur);
            String destinationKey = String.format("%s/%s", finalPath, ur.getName());
            fd.setPath(destinationKey);
            fd.setName(ur.getOriginalFilename());
            return fd;
        }).collect(Collectors.toList());
    }

    private AliOssClient getAliOssClient() {
        if (aliOssClient == null) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "未配置AliOss或配置错误");
        }
        return aliOssClient;
    }
}
