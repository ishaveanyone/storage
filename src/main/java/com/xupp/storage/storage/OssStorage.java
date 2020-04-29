package com.xupp.storage.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;

import com.xupp.storage.ApplicationContextProvider;
import com.xupp.storage.storage.config.MongoStorageConfig;
import com.xupp.storage.storage.config.OssStorageConfig;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import static com.xupp.storage.storage.IStorage.checkSpaceLegal;

public class OssStorage implements IStorage {

    private OSS ossClient;


    private OssStorageConfig ossStorageConfig;
    {
        ossStorageConfig=ApplicationContextProvider.getBean(OssStorageConfig.class);
    }

    @Override
    public IStorage connect(){
        if(ossStorageConfig==null){
            ossStorageConfig= ApplicationContextProvider.getBean(OssStorageConfig.class);
        }
        ossClient=new OSSClientBuilder().build(ossStorageConfig.getEndpoint(),
                ossStorageConfig.getAccessKeyId(),
                ossStorageConfig.getAccessKeySecret());
        return this;
    }

    @Override
    public boolean uploadFile(String targetKey,File  file) throws IOException {
        String bucketName = ossStorageConfig.getBucketName();
        return uploadFileToSpace(bucketName,targetKey,file);
    }


    @Override
    public boolean uploadFileToSpace(String space,String targetKey,File file) throws IOException {
        space= checkSpaceLegal(space,ossStorageConfig.getBucketName());
        InputStream inputStream=new FileInputStream(file);
        if(!ossClient.doesBucketExist(space)){
            //如果不存在那么就直接创建
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(space);
            ossClient.createBucket(createBucketRequest);
            //设置成公共读写
            createBucketRequest.setCannedACL(CannedAccessControlList.PublicReadWrite);
        }
        String objectName = targetKey;//这个是 云上的放的路径
        byte[] bytes=new byte[(int)file.length()];
        inputStream.read(bytes);
        ossClient.putObject(space, objectName,
                new ByteArrayInputStream(bytes));
        return ossClient.getObject(space,objectName)!=null;
    }

    //使用追加文件的方式事现断点续传
    @Override
    public boolean uploadContinue(String space,Integer chunkNum,String targetKey, File file) throws Throwable {
        space= checkSpaceLegal(space,ossStorageConfig.getBucketName());
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("text/plain");
        //先得判断是不是存在文件
        if(!ossClient.doesObjectExist(space,targetKey)){
            //追加文件
            AppendObjectRequest appendObjectRequest =
                    new AppendObjectRequest(space, targetKey,file);
            appendObjectRequest.setPosition(0L);//初始文件那么在开始位置进行文件添加
            AppendObjectResult appendObjectResult=
                    ossClient.appendObject(appendObjectRequest);
            return appendObjectResult!=null;
        }else{
            //
            AppendObjectRequest appendObjectRequest =
                    new AppendObjectRequest(space, targetKey,file);
//            OSSObject ossObject= ossClient.getObject(space,targetKey);
            SimplifiedObjectMeta simplifiedObjectMeta =
                    ossClient.getSimplifiedObjectMeta(space,targetKey);
            appendObjectRequest.setPosition(simplifiedObjectMeta.getSize());//初始文件那么在开始位置进行文件添加
            AppendObjectResult appendObjectResult=
                    ossClient.appendObject(appendObjectRequest);
            return appendObjectResult!=null;
        }
    }

    @Override
    public InputStream downloadFile(String targetKey) {
        return null;
    }


    @Override
    public InputStream downloadFileFromSpace(String space,String targetKey) {
        //
        space= checkSpaceLegal(space,ossStorageConfig.getBucketName());
        OSSObject ossObject = ossClient.getObject(space, targetKey);
        return ossObject.getObjectContent();
    }

    public InputStream downloadContinue(String targetKey,String fileName) throws Throwable {
        return null;
    }


    @Override
    public boolean removeFile(String space,String targetKey) {
        space= checkSpaceLegal(space,ossStorageConfig.getBucketName());
        ossClient.deleteObject( space, targetKey);
        return ossClient.doesObjectExist( space, targetKey);
    }

    @Override
    public boolean removeFiles(String space, List<String> keys) {
        space= checkSpaceLegal(space,ossStorageConfig.getBucketName());
        DeleteObjectsRequest deleteObjectsRequest =new DeleteObjectsRequest(space);
        deleteObjectsRequest.setKeys(keys);
        DeleteObjectsResult deleteObjectsResult= ossClient.deleteObjects(deleteObjectsRequest);
        return  keys.containsAll(deleteObjectsResult.getDeletedObjects());
    }

    @Override
    public Long uploadedSize(String space, String targerKey) throws IOException {
        space= checkSpaceLegal(space,ossStorageConfig.getBucketName());

       //是不是使用 异步上传的
        ObjectListing objectList=ossClient.listObjects(space,targerKey+"_");
//        objectList.getNextMarker()
        List<OSSObjectSummary> ossObjectSummaries=
                objectList.getObjectSummaries();
        if(!ossObjectSummaries.isEmpty()){
            //如果非空的话 那么 就查找所有的数据 并且 进行
            long total=0L;
            for (OSSObjectSummary ossObjectSummary : ossObjectSummaries) {
                SimplifiedObjectMeta simplifiedObjectMeta=  ossClient.getSimplifiedObjectMeta(space,ossObjectSummary.getKey());
                total+=simplifiedObjectMeta.getSize();
            }
            return total;
        }
        if(!ossClient.doesObjectExist(space,targerKey)){
            return 0L;
        }
        SimplifiedObjectMeta simplifiedObjectMeta=  ossClient.getSimplifiedObjectMeta(space,targerKey);
        return  simplifiedObjectMeta.getSize();
    }

    @Override
    public void close() throws RuntimeException {
        if(ossClient!=null){
            ossClient.shutdown();
        }else {
            throw new RuntimeException("未初始化客户端");
        }

    }


    @Override
    public Boolean merge(String space, String targerKey,String filename)  {
        space= checkSpaceLegal(space,ossStorageConfig.getBucketName());
        if(ossClient.doesObjectExist(space,targerKey)){
            //如果存在那么就不用合并了
            System.out.println("使用同步的情况不需要进行数据合并");
            return true;
        }
        ObjectListing objectList=ossClient.listObjects(space,targerKey+"_");
        List<OSSObjectSummary> ossObjectSummaries=
                objectList.getObjectSummaries();
        ossObjectSummaries.sort((o1,o2)->{
            //进行文件的排序然后进行文件的上传 操作
            String k1=o1.getKey();
            Integer i1=Integer.valueOf(k1.substring(k1.lastIndexOf((targerKey+"_").length())));
            String k2=o2.getKey();
            Integer i2=Integer.valueOf(k2.substring(k2.lastIndexOf((targerKey+"_").length())));
            return i1.compareTo(i2);
        });
        //追加文件
        try {
            for (OSSObjectSummary ossObjectSummary : ossObjectSummaries) {
                OSSObject ossObject =
                        ossClient.getObject(space, ossObjectSummary.getKey());
                InputStream stream = ossObject.getObjectContent();
                File tempFile = Files.createTempFile("oss", "").toFile();
                FileUtils.copyInputStreamToFile(stream, tempFile);
                AppendObjectRequest appendObjectRequest =
                        new AppendObjectRequest(space, targerKey, tempFile);
                //            OSSObject ossObject= ossClient.getObject(space,targetKey);
                SimplifiedObjectMeta simplifiedObjectMeta =
                        ossClient.getSimplifiedObjectMeta(space, targerKey);
                appendObjectRequest.setPosition(simplifiedObjectMeta.getSize());//初始文件那么在开始位置进行文件添加
                ossClient.appendObject(appendObjectRequest);
                //删除文件
                ossClient.deleteObject(space, ossObjectSummary.getKey());
                tempFile.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        // 给文件设置标签。
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("filename", filename);
        ossClient.setObjectTagging(space, targerKey, tags);
        return true;
    }


    /**
     * 本来想和mongo 一样使用 tag 进行 打标签然后 进行 搜索的，结果 oss 没有提供 标签的搜索api （todo 可能是自己没有发现 ）
     * @param space
     * @param chunkNum
     * @param targetKey
     * @param file
     * @return
     * @throws Throwable
     *
     */
    @Override
    public Future<Boolean> uploadContinueAsyc(String space, Integer chunkNum, String targetKey, File file) throws Throwable {
        space= checkSpaceLegal(space,ossStorageConfig.getBucketName());
        InputStream inputStream=new FileInputStream(file);
        if(!ossClient.doesBucketExist(space)){
            //如果不存在那么就直接创建
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(space);
            ossClient.createBucket(createBucketRequest);
            //设置成公共读写
            createBucketRequest.setCannedACL(CannedAccessControlList.PublicReadWrite);
        }
        String objectName = targetKey+"_"+chunkNum;//这个是 云上的放的路径
        byte[] bytes=new byte[(int)file.length()];
        inputStream.read(bytes);
        ossClient.putObject(space, objectName,
                new ByteArrayInputStream(bytes));
        return new AsyncResult( ossClient.getObject(space,objectName)!=null);
    }

}
