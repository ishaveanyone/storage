package com.xupp.storage.facator;



import com.xupp.storage.storage.IStorage;
import com.xupp.storage.storage.StorageBuilder;
import com.xupp.storage.storage.StorageServerEnum;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class StorageServerFactor {
    @Value("${storage.type}")
    private String type;

    public IStorage getStorageServer(String type){
        return  getStorageServer(StorageServerEnum.getFileServerEnum(type));
    }
    //获取默认的 服务
    public IStorage getStorageServer(){
        return  getStorageServer(StorageServerEnum.getFileServerEnum(type));
    }

    public IStorage getStorageServer(StorageServerEnum fileServerEnum){
        return new StorageBuilder().build(fileServerEnum).connect();
    }

}
