/**
 * Company: 上海数慧系统技术有限公司
 * Department: 数据中心
 * Date: 2019/10/22
 * Author: chenyp@dist.com.cn
 * Desc:
 */
package com.xupp.storage.exception;


import com.xupp.storage.define.HttpStatusEnum;

public abstract class AbstractException extends RuntimeException{

    HttpStatusEnum status ;

    public AbstractException(String message){
        super(message);
        status = HttpStatusEnum.FAIL;
    }

    public AbstractException(String message, HttpStatusEnum status){
        this(message);
        this.status = status;
    }

    public HttpStatusEnum getStatus(){
        return this.status;
    }

}