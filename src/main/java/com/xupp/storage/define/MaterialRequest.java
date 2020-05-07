
package com.xupp.storage.define;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;

@Data
public class MaterialRequest {
    //关联主体的 id （比如项目）
    private String refId;
    //文件名称
    private String name;
    //父级id
    private String parentGuid;
    //存储空间域
    private String space;

    @JsonIgnore
    private String creator; // 不必要的参数

    //这个是一个可扩展的标签
    private Map<String,String> tags;

}
