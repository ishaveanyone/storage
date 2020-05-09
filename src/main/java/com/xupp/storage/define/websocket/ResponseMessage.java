
package com.xupp.storage.define.websocket;

import lombok.Data;

/**
 * 服务端发送客户端的消息
 */
@Data
public class ResponseMessage {

    Object data;//返回的数据

    String message;// 返回给前端的具体消息


    public ResponseMessage(Object data, String message) {
        this.data = data;
        this.message = message;
    }
    public ResponseMessage(Object data) {
        this.data = data;
        this.message = "success";
    }
    public ResponseMessage() {

    }
}
