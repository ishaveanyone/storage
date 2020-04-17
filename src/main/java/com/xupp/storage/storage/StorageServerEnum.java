package com.xupp.storage.storage;

public enum StorageServerEnum {
    OSS("OSS"),
    LOCAL("LOCAL"),//使用项目文件路径作为存储路径
    FTP("FTP"),
    MONGO("MONGO");
    private String type;
    StorageServerEnum(String type){
        this.type=type;
    }
    public String getType() {
        return type.toLowerCase();
    }
    public static StorageServerEnum getFileServerEnum(String type){
        type=type.toUpperCase();
        StorageServerEnum result = null;
        for(StorageServerEnum item : values()){
            if(type.equals(item.type)){
                result = item;
                break;
            }
        }
        if(null == result){
            throw new RuntimeException("未定义文件服务类型:{}" + type);
        }
        return result;
    }
}
