package com.nowcoder.community.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
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
    //根据数据生成JSON字符串
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject jsonObject =new JSONObject();
        jsonObject.put("code",code);
        jsonObject.put("msg",msg);
        if(map!=null){
            for(String key:map.keySet()){
                jsonObject.put(key,map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }
    public static String getJSONString(int code, String msg){
        return getJSONString(code,msg,null);
    }
    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }
}
