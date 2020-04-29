
package com.xupp.storage.storage;


import com.mongodb.*;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.*;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.xupp.storage.ApplicationContextProvider;
import com.xupp.storage.storage.config.MongoStorageConfig;
import com.xupp.storage.storage.keygenerator.MongoKeyGenerate;
import org.apache.commons.collections4.IteratorUtils;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import static com.xupp.storage.storage.IStorage.checkSpaceLegal;

//version mongo-3.4版本
public class MongoStorage  implements IStorage {

    private MongoClient mongoClient;

    //不要使用依赖注入 如果使用依赖注入那么新创建的

    private MongoStorageConfig mongoStorageConfig;

    {
        mongoStorageConfig=ApplicationContextProvider.getBean(MongoStorageConfig.class);
    }


    @Override
    public IStorage connect() {
        //如果使用多例
        if(mongoStorageConfig==null){
            mongoStorageConfig= ApplicationContextProvider.getBean(MongoStorageConfig.class);
        }
        if("true".equalsIgnoreCase(mongoStorageConfig.getAuthEnable())){
            connectAuth();
        }else if("false".equalsIgnoreCase(mongoStorageConfig.getAuthEnable())){
            connectUnAuth();
        }else {
            throw new RuntimeException("配置出错,请检查 启动mongo 是否需要加密登录配置");
        }
        return this;
    }
    //加密登录
    private void  connectAuth(){

        //连接到MongoDB服务 如果是远程连接可以替换“localhost”为服务器所在IP地址
        //ServerAddress()两个参数分别为 服务器地址 和 端口
        ServerAddress serverAddress = new ServerAddress(mongoStorageConfig.getHost(),mongoStorageConfig.getPort());
        List<ServerAddress> addrs = new ArrayList<ServerAddress>();
        addrs.add(serverAddress);
        //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
        MongoCredential credential =
                MongoCredential.createScramSha1Credential(mongoStorageConfig.getUserName(),
                        mongoStorageConfig.getAuthDatabase(),
                        mongoStorageConfig.getPassWord().toCharArray());
        List<MongoCredential> credentials = new ArrayList<MongoCredential>();
        credentials.add(credential);
        mongoClient = new MongoClient(addrs,credentials);
    }

    //登录的时候不需要密码
    private void connectUnAuth(){
        ServerAddress serverAddress = new ServerAddress(mongoStorageConfig.getHost(),
                mongoStorageConfig.getPort());
        List<ServerAddress> addrs = new ArrayList<ServerAddress>();
        addrs.add(serverAddress);
        //不需要进行加密连接的方式
        mongoClient = new MongoClient(addrs);
    }

    @Override
    public boolean uploadFile(String targetKey, File file)
            throws IOException {
        return false;
    }

    @Override
    public boolean uploadFileToSpace(String space, String targetKey, File file)
            throws IOException {
       /* space= checkSpaceLegal(space,mongoStorageConfig.getCollection());
        MongoDatabase db = mongoClient.getDatabase(mongoStorageConfig.getDatabase());
        GridFSBucket bucket = GridFSBuckets.create(db,space); // 如果这里不指定 集合 也就是 bucket 那么默认就是 fs
        BsonValue id= new BsonString(targetKey);
        bucket.uploadFromStream(id,targetKey, new FileInputStream(file));
        Bson query=Filters.eq("_id", id.asString().getValue());
        return bucket.find(query).iterator().hasNext(); // 查找文件是否存在*/

        space= checkSpaceLegal(space,mongoStorageConfig.getCollection());
        MongoDatabase db = mongoClient.getDatabase(mongoStorageConfig.getDatabase());
        GridFSBucket bucket = GridFSBuckets.create(db,space);
        BsonValue id= MongoKeyGenerate.getInstance().generateKey();
        Document metadata=new Document();
        GridFSUploadOptions gridFSUploadOptions=new GridFSUploadOptions();
        metadata.put("refid",targetKey);
        gridFSUploadOptions.metadata(metadata);
        bucket.uploadFromStream(id,file.getName(), new FileInputStream(file),gridFSUploadOptions);
        Bson query=Filters.eq("_id", id);
        return bucket.find(query).iterator().hasNext();
    }



    @Override
    public InputStream downloadFile(String targetKey) {
        return null;
    }

    @Override
    public InputStream downloadFileFromSpace(String space, String targetKey) {
        space= checkSpaceLegal(space,mongoStorageConfig.getCollection());
        MongoDatabase db = mongoClient.getDatabase(mongoStorageConfig.getDatabase());
        GridFSBucket bucket = GridFSBuckets.create(db,space);
        GridFSFindIterable gridFSFiles=
                bucket.find(Filters.eq("metadata.refid", targetKey));
        MongoCursor<GridFSFile> mongoCursor= gridFSFiles.iterator();
        if(mongoCursor.hasNext()){
            return bucket.openDownloadStream(mongoCursor.next().getId());
        }
        return null;
    }

    @Override
    public InputStream downloadContinue(String targetKey, String fileName) throws Throwable {
        return null;
    }

    @Override
    public boolean removeFile(String space, String key) {
        space= checkSpaceLegal(space,mongoStorageConfig.getCollection());
        MongoDatabase db = mongoClient.getDatabase(mongoStorageConfig.getDatabase());
        GridFSBucket bucket = GridFSBuckets.create(db,space);
        GridFSFindIterable gridFSFiles=
                bucket.find(Filters.eq("metadata.refid", key));
        MongoCursor<GridFSFile> mongoCursor= gridFSFiles.iterator();
        if(!mongoCursor.hasNext()){
            return true;
        }
        bucket.delete(mongoCursor.next().getId());
        return  !bucket.find(Filters.eq("metadata.refid", key)).iterator().hasNext();
    }

    @Override
    public boolean removeFiles(String space, List<String> keys) {
        space= checkSpaceLegal(space,mongoStorageConfig.getCollection());
        AtomicBoolean flag=new AtomicBoolean(true);
        for (String key : keys) {
            flag.set(removeFile(space,key));
        }
        return flag.get();
    }

    @Override
    public boolean uploadContinue(String space, Integer chunkNum,
                                               String targetKey, File file)
            throws Throwable {
        space= checkSpaceLegal(space,mongoStorageConfig.getCollection());
        MongoDatabase db = mongoClient.getDatabase(mongoStorageConfig.getDatabase());
        GridFSBucket bucket = GridFSBuckets.create(db,space); // 如果这里不指定 集合 也就是 bucket 那么默认就是 fs
        BsonValue id=MongoKeyGenerate.getInstance().generateKey();
        GridFSUploadOptions gridFSUploadOptions=new GridFSUploadOptions();
        Document metadata=new Document();
        metadata.put("refid",targetKey);
        metadata.put("chunkNum",chunkNum);
        gridFSUploadOptions.metadata(metadata);
        GridFSUploadStream uploadStreamStream =bucket.openUploadStream(id,targetKey,gridFSUploadOptions);
        ReadableByteChannel finChannel=
                Channels.newChannel(new FileInputStream(file));
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
        while (finChannel.read(byteBuffer)!=-1){//先读取上一次的
            //翻转指针
            byteBuffer.flip();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            uploadStreamStream.write(bytes);
            byteBuffer.clear();
        }
        uploadStreamStream.close();
        Bson query=Filters.eq("_id", targetKey);
        return bucket.find(query).iterator().hasNext();
    }

    @Override
    public Future<Boolean> uploadContinueAsyc(String space, Integer chunkNum, String targetKey, File file) throws Throwable {
        space= checkSpaceLegal(space,mongoStorageConfig.getCollection());
        MongoDatabase db = mongoClient.getDatabase(mongoStorageConfig.getDatabase());
        GridFSBucket bucket = GridFSBuckets.create(db,space); // 如果这里不指定 集合 也就是 bucket 那么默认就是 fs
        BsonValue id=MongoKeyGenerate.getInstance().generateKey();
        GridFSUploadOptions gridFSUploadOptions=new GridFSUploadOptions();
        Document metadata=new Document();
        metadata.put("refid",targetKey);
        metadata.put("chunkNum",chunkNum);
        gridFSUploadOptions.metadata(metadata);
        GridFSUploadStream uploadStreamStream =bucket.openUploadStream(id,targetKey,gridFSUploadOptions);
        ReadableByteChannel finChannel=
                Channels.newChannel(new FileInputStream(file));
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
        while (finChannel.read(byteBuffer)!=-1){//先读取上一次的
            //翻转指针
            byteBuffer.flip();
            //remaining = limit - position
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            uploadStreamStream.write(bytes);
            //清空buffer
            byteBuffer.clear();
        }
        uploadStreamStream.close();
        Bson query=Filters.eq("_id", targetKey);
        return new AsyncResult<Boolean>(bucket.find(query).iterator().hasNext());
    }

    @Override
    public Long uploadedSize(String space, String targerKey) {
        Long result=0L;
        space= checkSpaceLegal(space,mongoStorageConfig.getCollection());
        MongoDatabase db = mongoClient.getDatabase(mongoStorageConfig.getDatabase());
        GridFSBucket bucket = GridFSBuckets.create(db,space); // 如果这里不指定 集合 也就是 bucket 那么默认就是 fs
        //模糊匹配对应的
        Bson query=Filters.regex("metadata.refid",targerKey);
        MongoCursor<GridFSFile> cursor= bucket.find(query).iterator();
        while(cursor.hasNext()){
            result+=cursor.next().getLength();
        }
        return result;
    }

    @Override
    public void close() throws RuntimeException {
        mongoClient.close();
    }



    @Override
    public Boolean merge(String space, String targerKey,String filename)  {
        space= checkSpaceLegal(space,mongoStorageConfig.getCollection());
        MongoDatabase db = mongoClient.getDatabase(mongoStorageConfig.getDatabase());
        GridFSBucket bucket = GridFSBuckets.create(db,space); // 如果这里不指定 集合 也就是 bucket 那么默认就是 fs

        //模糊匹配对应的
        Bson query=Filters.regex("metadata.refid",targerKey);
        MongoCursor<GridFSFile> cursor= bucket.find(query).iterator();
        List<GridFSFile> list= IteratorUtils.toList(cursor);
        list.sort((o1,o2)->{
            //通过 下标进行排序  保证还原的时候不会出现乱序情况
            Integer i1=(Integer)  o1.getMetadata().get("chunkNum");
            Integer i2=(Integer)  o2.getMetadata().get("chunkNum");
            return i1.compareTo(i2);
        });
        //进行数据合并
        BsonValue id= MongoKeyGenerate.getInstance().generateKey();
        GridFSUploadOptions gridFSUploadOptions=new GridFSUploadOptions();
        Document metadata=new Document();
        metadata.put("refid",targerKey);
        gridFSUploadOptions.metadata(metadata);
        GridFSUploadStream uploadStreamStream =bucket.openUploadStream(id,filename,gridFSUploadOptions);
        for (GridFSFile gridFSFile : list) {
            GridFSDownloadStream  downloadStream=bucket.openDownloadStream(gridFSFile.getId());
            ReadableByteChannel inChannel=
                    Channels.newChannel(downloadStream);
            ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
            try {
                while (inChannel.read(byteBuffer) != -1) {//先读取上一次的
                    //翻转指针
                    byteBuffer.flip();
                    //remaining = limit - position
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    uploadStreamStream.write(bytes);
                    //清空buffer
                    byteBuffer.clear();
                }
            }catch (Exception e){
                return false;
            }
        }
        uploadStreamStream.close();
        MongoCursor<GridFSFile> cursor2= bucket.find(query).iterator();
        while(cursor2.hasNext()){
            GridFSFile fsFile=  cursor2.next();
            if(fsFile.getMetadata().containsKey("chunkNum")){
                bucket.delete(fsFile.getId());
            }
        }
        return true;
    }


}
