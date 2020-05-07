/**
 * @date 2019/12/12
 * @author dingchw
 * @email dingchw@dist.com.cn
 * @desc
 */
package com.xupp.storage.define;

public enum HttpStatusEnum {
    NORMAL("请求成功",1000),
    FAIL("业务逻辑错误，请求失败",2000),
    ERROR("系统内部错误, 请求失败", 3000),
    VALIDHTTP("请求无效",4000),
    UNAUTH("无访问权限", 4001),
    FORBIDDEN("无访问权限", 4003),
    NOTFOUND("找不到资源", 4004),
    MUSTWRITE("属性为必填项", 4005);



    private String message;
    private Integer code;

    HttpStatusEnum(String message, Integer code){
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }
    public String getMessage(){
        return message;
    }
}
