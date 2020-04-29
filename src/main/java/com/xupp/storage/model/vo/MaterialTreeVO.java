
package com.xupp.storage.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class MaterialTreeVO implements Serializable {
    private String nodeCode;

    private String nodeName;

    private Date createTime;

    private Integer nodeType;

    private String parentCode;

    @JsonIgnore
    private String creator;

    private String tags;

    private Integer state;

    private List<MaterialTreeVO> children;

    @JsonIgnore
    private String space;
}
