package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class FileUploadRequest {
    @NotNull
    private MultipartFile file;
}
