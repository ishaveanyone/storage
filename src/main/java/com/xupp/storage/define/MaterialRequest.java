
package com.xupp.storage.define;

import lombok.Data;

import java.util.Map;

@Data
public class MaterialRequest {
    private String refId;

    private String name;

    private String parentGuid;

    private String space;

    //标签列表
    private Map<String,String> tags;

}
