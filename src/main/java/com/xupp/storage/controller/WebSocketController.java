/**
 * Company: 上海数慧系统技术有限公司
 * Department: 数据中心
 * Date: 2020-01-10 10:04
 * Author: xupp
 * Email: xupp@dist.com.cn
 * Desc：
 */

package com.xupp.storage.controller;

import com.xupp.storage.define.Constants;

import com.xupp.storage.define.websocket.ResponseMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @SubscribeMapping(Constants.WebSocket.QUEUE+"/process/{guid}")
    public Object subProcess(@DestinationVariable String guid) throws Exception {
        return new ResponseMessage("获取进度条服务订阅成功", "success");
    }




    /*******************下面的代码是测试使用的 ********************************/
    @MessageMapping("/hello/{guid}")
    public Object say(@DestinationVariable String guid) throws Exception {
        System.out.println("前端请求被接受到");
        int i=0;
        while(i<10){
            Thread.sleep(1000);
            i++;
//            messagingTemplate.convertAndSend("/queue/getResponse/"+guid,"hello"+i);
        }
        return "数据发送完毕";
    }
    @SubscribeMapping("/queue/getResponse/{guid}")
    public Object sub(
            @DestinationVariable String guid
    ) throws InterruptedException {
        System.out.println("感谢"+guid+"订阅");
        return "ok";
    }
}