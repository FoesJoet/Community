package com.nowcoder.community.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * Developer：Foes
 */
public class CommunityUtil {
    //随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString();
    }
    //MD5加密
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }else{
            return DigestUtils.md5DigestAsHex(key.getBytes());
        }

    }
}
