package icu.lowcoder.spring.cloud.authentication.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddWeAppBindingRequest {
    private String appId; // 小程序 appId
    private String jsCode; // 授权码
    private String encryptedData; // 用户信息加密数据
    private String iv; // 用于解密数据的向量
}
