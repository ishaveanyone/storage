
package com.xupp.storage.define;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

@Data
@Entity(name="ers_material")
public class ErsMaterialEntity {
    @Id
    protected String guid = uuid();

    protected String name;

    protected Date createTime = new Date(System.currentTimeMillis());

    protected String creator;

    public String uuid(){
        return UUID.randomUUID().toString();
    }

    //外键ID
    private String refId;

    private String parentGuid;

    //节点类型 0表示文件夹，1表示文件
    private Integer nodeType;

    // 空间域
    private String space;

    //对每一个文件可以打标签 key value 的标签
    private String tags;

    //文件文件夹状态 0正常 1上传 2删除
    private Integer state=1;

}
