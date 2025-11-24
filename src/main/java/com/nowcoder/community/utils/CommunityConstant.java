package com.nowcoder.community.utils;

/**
 * Developer：Foes
 */
public interface CommunityConstant {
    //激活状态
    int ACTIVATION_SUCCESS =1;

    int ACTIVATION_FAIl =0;

    int ACTIVATION_REPEATED =2;
    //登录凭证存续时间
    int DEFAULT_EXPIRED_SECOND =3600*12;

    int REMEMBER_EXPIRED_SECOND =3600*24*90;
    //评论实体类型
    int ENTITY_TYPE_POST =1;

    int ENTITY_TYPE_COMMENT=2;
}
