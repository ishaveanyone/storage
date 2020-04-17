

package com.xupp.storage.storage.fileconvertor;


import com.xupp.storage.ApplicationContextProvider;

public class FileConvertorBuilder {

    private  JacobFileConvertor jacobFileConvertor= ApplicationContextProvider.getBean(JacobFileConvertor.class); //事现配置好一些bena

    private  KkfileviewFileConvertor kkfileviewFileConvertor= ApplicationContextProvider.getBean(KkfileviewFileConvertor.class); //事现配置好一些bena

    //默认非单例
    private boolean single=false;

    public FileConvertorBuilder setSingle(boolean single) {

        this.single = single;
        return this;
    }

    public  IFileConvertor build(FileConvertorEnum fileConvertorEnum){

        switch (fileConvertorEnum){
            case JACOB:
                return !single?new JacobFileConvertor():jacobFileConvertor;
            case KKFILEVIEW:
                return !single?new KkfileviewFileConvertor():kkfileviewFileConvertor;
            default:
                throw  new RuntimeException("未定义预览服务端类型");
        }
    }

}
