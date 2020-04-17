

package com.xupp.storage.storage.fileconvertor;

public enum FileConvertorEnum {

    JACOB("JACOB"),
    KKFILEVIEW("KKFILEVIEW");//使用项目文件路径作为存储路径
    private String type;

    public String getType() {
        return type;
    }

    FileConvertorEnum(String type){
        this.type=type;
    }

    public static FileConvertorEnum getFileConvertorEnum(String type){
        type=type.toUpperCase();
        FileConvertorEnum result = null;
        for(FileConvertorEnum item : values()){
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
