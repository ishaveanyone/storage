

package com.xupp.storage.model.dto;

import lombok.Data;

import java.util.Map;

@Data
public class MaterialQueryDTO {
    //所属存储域
    private String space;

    //父级id 如果是一级父级id 就是 -1
    private String parentid;

    // 文件挂在哪一个项目上面
    private String refid;

    // 文件名
    private String fileName;

    //文件标签
    private Map<String,String> tags;


}
