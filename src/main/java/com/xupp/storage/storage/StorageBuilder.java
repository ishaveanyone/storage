package com.xupp.storage.storage;


import com.xupp.storage.SorageApplicationContextProvider;

public class StorageBuilder {

    private  MongoStorage mongoStorage=
            SorageApplicationContextProvider.getBean(MongoStorage.class);//事现配置好一些bena


    private  OssStorage ossStorage=
            SorageApplicationContextProvider.getBean(OssStorage.class);




    //默认非单例
    private Boolean single=false; //非静态外部属性 不存在现成

    public  StorageBuilder setSingle(boolean single) {
        this.single = single;
        return this;
    }

    public  IStorage build(StorageServerEnum storageServerEnum){
        switch (storageServerEnum) {
            case FTP:
                return null;
            case LOCAL:
                return null;
            case MONGO:
                return !single?new MongoStorage():mongoStorage;
            case OSS:
                return !single?new OssStorage():ossStorage;
            default:
                throw new RuntimeException("未定义文件服务类型");
        }
    }
}
