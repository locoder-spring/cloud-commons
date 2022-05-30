package icu.lowcoder.spring.cloud.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AliOssBucket {
    private String name;
    private Date createDate;
    private String location;
    private String extranetEndpoint;
    private String intranetEndpoint;
}
