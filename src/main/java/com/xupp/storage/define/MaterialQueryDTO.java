

package com.xupp.storage.define;

import lombok.Data;

import java.util.Map;

@Data
public class MaterialQueryDTO {

    private String space;//所属存储域
    private String parentid;//父级id 如果是一级父级id 就是 -1
    private String refid; // 文件挂在哪一个项目上面
    private String fileName; // 文件名
    private Map<String,String> tags; //文件标签


}
