package icu.lowcoder.spring.cloud.content.manager;


import icu.lowcoder.spring.cloud.content.dto.GenericAttachment;
import icu.lowcoder.spring.cloud.content.feign.CommonsFilesClient;
import icu.lowcoder.spring.cloud.content.feign.model.FileAcl;
import icu.lowcoder.spring.cloud.content.feign.model.FileDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttachmentManager {
    @Autowired
    private CommonsFilesClient commonsFilesClient;

    public <A> List<A> merge(List<A> attachments, Class<A> attachmentType, List<GenericAttachment> requestList, String dir, FileAcl acl) {
        List<A> merged = new ArrayList<>();

        if (requestList != null && !requestList.isEmpty()) {
            if(requestList.stream().filter(Objects::nonNull).anyMatch(ra -> ra.getId() == null && ra.getFileId() != null)) {
                List<FileDescription> newFiles = new ArrayList<>(commonsFilesClient.saveTmpFile(
                        requestList
                                .stream()
                                .filter(ra -> ra.getId() == null && ra.getFileId() != null)
                                .map(GenericAttachment::getFileId)
                                .toArray(UUID[]::new),
                        dir,
                        acl
                ));

                // new add
                merged.addAll(newFiles.stream().map(f -> {
                    A attachment = null;
                    try {
                        attachment = attachmentType.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if (attachment != null) {
                        Field nameField = ReflectionUtils.findField(attachmentType, "name");
                        ReflectionUtils.makeAccessible(nameField);
                        ReflectionUtils.setField(nameField, attachment, f.getName());

                        Field pathField = ReflectionUtils.findField(attachmentType, "path");
                        ReflectionUtils.makeAccessible(pathField);
                        ReflectionUtils.setField(pathField, attachment, f.getPath());

                        Field sizeField = ReflectionUtils.findField(attachmentType, "size");
                        ReflectionUtils.makeAccessible(sizeField);
                        ReflectionUtils.setField(sizeField, attachment, f.getSize());
                    }
                    return attachment;
                }).collect(Collectors.toList()));
            }

            // removed
            if (attachments != null&&attachments.size()>0) {
                merged.addAll(attachments.stream().filter(a -> {
                    Field idField = ReflectionUtils.findField(attachmentType, "id", UUID.class);
                    ReflectionUtils.makeAccessible(idField);
                    UUID id = (UUID) ReflectionUtils.getField(idField, a);
                    return requestList.stream().filter(ra -> ra.getId() != null).anyMatch(ra -> ra.getId().equals(id));
                }).collect(Collectors.toList()));
            }
        }

        return merged;
    }

    public <A> List<A> merge(List<A> attachments, Class<A> attachmentType, List<GenericAttachment> requestList, String dir) {
        return merge(attachments, attachmentType, requestList, dir, null);
    }

    public <A> List<GenericAttachment> mapVo(List<A> attachments, Class<A> attachmentType) {
        if (attachments != null && !attachments.isEmpty()) {
            return attachments.stream().map(a -> {

                Field nameField = ReflectionUtils.findField(attachmentType, "name");
                Field pathField = ReflectionUtils.findField(attachmentType, "path");
                Field idField = ReflectionUtils.findField(attachmentType, "id", UUID.class);
                Field sizeField = ReflectionUtils.findField(attachmentType, "size");
                assert nameField != null;
                ReflectionUtils.makeAccessible(nameField);
                assert pathField != null;
                ReflectionUtils.makeAccessible(pathField);
                assert idField != null;
                ReflectionUtils.makeAccessible(idField);
                assert sizeField != null;
                ReflectionUtils.makeAccessible(sizeField);
                return GenericAttachment.builder()
                        .id((UUID) ReflectionUtils.getField(idField, a))
                        .name((String) ReflectionUtils.getField(nameField, a))
                        .size((Long) ReflectionUtils.getField(sizeField, a))
                        .path((String) ReflectionUtils.getField(pathField, a)).build();
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
