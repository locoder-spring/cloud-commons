package icu.lowcoder.spring.cloud.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericAttachment {
    private UUID id;
    private UUID fileId;
    private String path;
    private String name;
    private Long size;
}