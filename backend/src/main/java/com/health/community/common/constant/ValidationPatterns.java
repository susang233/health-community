package com.health.community.common.constant;

public class ValidationPatterns {
    // 密码正则：字母+数字+特殊字符，8-15位
    public static final String PASSWORD_REGEX =
            "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+]).{8,15}$";
    public static final String PASSWORD_MSG =
            "密码必须包含字母、数字和特殊字符，长度8-15位";



    // 账号正则：字母数字，5-15位
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9]{5,15}$";
    public static final String USERNAME_MSG = "账号只能包含字母和数字，长度5-15位";

    // 昵称：2-20位，不能有特殊字符
    public static final String NICKNAME = "^[\\u4e00-\\u9fa5a-zA-Z0-9]{2,20}$";
    public static final String NICKNAME_MSG = "昵称只能包含中文、字母和数字，2-20位";


}


