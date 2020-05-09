package com.xupp.storage.facade;


import com.alibaba.fastjson.JSONObject;
import com.xupp.storage.define.*;
import com.xupp.storage.exception.MaterialException;
import com.xupp.storage.facator.FileConvertorFactory;
import com.xupp.storage.facator.StorageServerFactor;
import com.xupp.storage.model.dto.MaterialQueryDTO;
import com.xupp.storage.model.vo.MaterialTreeVO;
import com.xupp.storage.model.vo.MaterialViewResultVO;
import com.xupp.storage.service.MaterialService;
import com.xupp.storage.storage.IStorage;
import com.xupp.storage.define.websocket.ResponseMessage;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class MaterialFacade {
    private Logger log= LoggerFactory.getLogger(MaterialFacade.class);

    @Autowired
    private MaterialService iMaterialService;

    @Value("${storage.temp_upload_path}")
    private String temp_upload_path;
    @Value("${storage.temp_download_path}")
    private String temp_download_path;

    @Value("${storage.convertor.server}")
    private String convertorServerPath;


    @Autowired
    StorageServerFactor storageServerFactor;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    FileConvertorFactory fileConvertorFactory;
    public String  getDownLoadPath(){
        return temp_download_path;
    }

    public String  getUploadPathPath(){
        return temp_upload_path;
    }
    /**
     * 添加文件夹 和 小文件
     * @param addRequest
     * @param type
     * @param request
     * @return
     */
    public MaterialTreeVO addMaterial(MaterialRequest addRequest,
                                      MaterialTypeEnum type,
                                      HttpServletRequest request)  {
        String space =null;
        MaterialTreeVO materialTreeVO =
                iMaterialService.addMaterial(addRequest, type);
        if (!MaterialTypeEnum.DIRECTORY.equals(type)) {
            try(IStorage iStorage = storageServerFactor.getStorageServer();) {
                materialTreeVO.setChildren(new ArrayList<MaterialTreeVO>());
                String key = materialTreeVO.getNodeCode();
                //目前需求中这块需要进行修改
                //需要进行文件上传
                if (type.equals(MaterialTypeEnum.FILE)) {
                    List<MultipartFile> files = ((MultipartHttpServletRequest) request)
                            .getFiles("file");
                    String localPath = request.getServletContext().getRealPath("/");
                    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
                    if (isMultipart) {
                        String tempFileDir = temp_upload_path;
                        File parentFileDir = new File(localPath + tempFileDir);
                        if (!parentFileDir.exists()) {
                            parentFileDir.mkdirs();
                        }
                        if (!files.isEmpty()) {
                            // 分片处理时，前台会多次调用上传接口，每次都会上传文件的一部分到后台
                            File targetFile = new File(parentFileDir, files.get(0).getOriginalFilename());
                            FileUtils.copyInputStreamToFile(files.get(0).getInputStream(), targetFile);
                            if(!iStorage.uploadFileToSpace(space,key, targetFile)){
                                //如果失败因为这个是小文件那么就直接删除
                                iMaterialService.deleteMaterial(key);
//                                throw new MaterialException.FileUploadFail(key);
                            }
                            //删除本地的临时文件
                            targetFile.delete();
                            iMaterialService.updateMaterialState(key, Constants.Material.NORMAL);
                        }
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return materialTreeVO;
    }

    /**
     * 删除文件列表
     * @param pspace
     * @param guids
     */
    public Boolean deleteMaterial(String pspace,List<String> guids){
        String space =null;
        Boolean flag= iMaterialService.mutiDeleteMaterial(guids);
        if(flag){
            deleteFile(space,guids);
        }
        return flag;
    }
    @Async
    void deleteFile(String space, List<String> guids){
        //如果删除数据库数据成功 那么 就删除 文件
        try(IStorage iStorage = storageServerFactor.getStorageServer();) {
            if(!iStorage.removeFiles(space,guids)){
                throw new MaterialException.FileDeleteFail(String.join(",",guids));
            }
        }catch (Exception e){
            throw  new RuntimeException("删除出错");
        }
    }

    /**
     * 预览文件
     * @param space
     * @param key
     * @param request
     * @return
     * @throws IOException
     */
    public MaterialViewResultVO previewMaterial(String space, String key, HttpServletRequest request) throws IOException {
        downloadMaterial(space,key,request);
        MaterialTreeVO materialTreeVO=iMaterialService.getMaterial(key);
        String httpPath = request.getScheme() + "://"
                + request.getServerName() + ":"
                + request.getServerPort()
                + request.getContextPath()+"/"+temp_download_path+ "/"+materialTreeVO.getNodeName();
        String downPath=request.getServletContext().getRealPath("/")+
                File.separatorChar+temp_download_path+File.separatorChar+materialTreeVO.getNodeName();

        return fileConvertorFactory.getFileConvertor().convert(downPath,
                httpPath,convertorServerPath);
    }

    /**
     * 下载文件
     * @param pspace
     * @param key
     * @param request
     * @return
     */
    public MaterialViewResultVO downloadMaterial(String pspace, String key, HttpServletRequest request) {
        String space=null;
        try (IStorage iStorage = storageServerFactor.getStorageServer();){
            String downPath = request.getServletContext().getRealPath("/") + "/" + temp_download_path;
            InputStream inputStream = iStorage.downloadFileFromSpace(space,key);
            MaterialTreeVO materialTreeVO = iMaterialService.getMaterial(key);
            String filePath = downPath + File.separator + materialTreeVO.getNodeName();
            FileUtils.copyInputStreamToFile(inputStream, new File(filePath));
            return new MaterialViewResultVO(new File(filePath).getAbsolutePath(), true);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 多文件下载
     * 返回对应的压缩包
     * @param
     * @param keys
     * @param relativePath 相对位置 用于组织打包文件加 内的 文件结构
     * @param request
     * @return
     */
    public MaterialViewResultVO mutiDownloadMaterial(String relativePath, List<String> keys,  HttpServletRequest request) {
        try (IStorage iStorage = storageServerFactor.getStorageServer();){
            for (String key : keys) {
                String downPath = request.getServletContext().getRealPath("/") + File.separatorChar + temp_download_path+
                        File.separatorChar+relativePath+File.separatorChar+key;

                MaterialTreeVO materialTreeVO = iMaterialService.getMaterial(key);
                String space=null;
                InputStream inputStream = iStorage.downloadFileFromSpace(space,key);
                String filePath = downPath + File.separator + materialTreeVO.getNodeName();
                FileUtils.copyInputStreamToFile(inputStream, new File(filePath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MaterialViewResultVO(new File(request.getServletContext().getRealPath("/") + File.separatorChar + temp_download_path+
                File.separatorChar+relativePath).getAbsolutePath(), true);
    }

    /**
     * 检索文件列表
     * @param materialQueryDTO
     * @return
     */
    public  List<MaterialTreeVO> getMaterialByLevel(MaterialQueryDTO materialQueryDTO){
        return iMaterialService.getMaterialByLevel(materialQueryDTO);
    }
    /**
     * 获取当前文件已经上传的进度  轮询方式
     */
    public Long checkUploaded(
            MaterialQueryDTO materialQueryDTO
    ){
        List<MaterialTreeVO> materials=getMaterialByLevel(materialQueryDTO);
        if(materials==null||materials.isEmpty()){
            return  0L;
        }
        if(materials.size()!=1){
            throw new RuntimeException();
        }
        String space =null;
        long redata=0L;
        MaterialTreeVO materialTreeVO= materials.get(0);
        Map<String,Object> maps= JSONObject.parseObject(materialTreeVO.getTags(),Map.class);
        Long fileSize=Long.valueOf( maps.get(Constants.Material.ATTRS.FILESIZE.alias).toString());

        try(IStorage iStorage = storageServerFactor.getStorageServer();){
            if(fileSize==redata){
                iMaterialService.updateMaterialState(materialTreeVO.getNodeCode(),Constants.Material.NORMAL);
            }
            redata= iStorage.uploadedSize(space,materialTreeVO.getNodeCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return redata;
    }

    /**
     * 将获取到的每一片文件上传oss
     */
    public Boolean addMaterialPartFile(
            HttpServletRequest request,
            String pspace,//存储的空间域
            Integer chunkNum,//分片编号
            String nodeGuid
    ){
        String space =null;
        //上传成功
        try(IStorage iStorage = storageServerFactor.getStorageServer();){
            List<MultipartFile> files = ((MultipartHttpServletRequest) request)
                    .getFiles("file");
            String localPath = request.getServletContext().getRealPath("/");
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                String tempFileDir = temp_upload_path;
                File parentFileDir = new File(localPath + tempFileDir+File.separator+chunkNum);
                if (!parentFileDir.exists()) {
                    parentFileDir.mkdirs();
                }
                if (!files.isEmpty()) {
                    // 分片处理时，前台会多次调用上传接口，每次都会上传文件的一部分到后台
                    File targetFile = new File(parentFileDir, files.get(0).getOriginalFilename());
                    FileUtils.copyInputStreamToFile(files.get(0).getInputStream(),targetFile);
                    if(iStorage.uploadContinue(space,chunkNum,nodeGuid,targetFile)){
                        return true;
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
        return true;
        //如果中间出现问题将文件对应文件修改为 上传中
    }

// websocket 返回数据 异步方式 直接返回
    @Async
    public void websocketCheckUploaded(
            String guid,
            MaterialQueryDTO materialQueryDTO
             ) throws InterruptedException {
//        强制死亡时间
        AtomicLong updeadtime=new AtomicLong(1*60*60L); //等待1小时
        AtomicLong ckdeadtime=new AtomicLong(100L); // 等待100秒
        List<MaterialTreeVO> materials=null;
        while(((materials=this.getMaterialByLevel(materialQueryDTO))==null||materials.isEmpty())&&ckdeadtime.get()>=0){
            Thread.sleep(1000);
//            messagingTemplate.convertAndSend(Constants.WebSocket.QUEUE+"/process/"+guid,UUID.randomUUID());
            System.out.println("检查中还没有数据 。。。。");

            ckdeadtime.decrementAndGet();
        }
        //轮查结束
        if(ckdeadtime.get()<0){
            return;
        }
        if(materials.size()>1){
//            throw new RuntimeException().FileMultiDataFail();
        }
        String space =null;
        MaterialTreeVO materialTreeVO= materials.get(0);
        Map<String,Object> maps=JSONObject.parseObject(materialTreeVO.getTags(),Map.class);
        AtomicLong redata=new AtomicLong(0);
        Long fileSize=Long.valueOf( maps.get(Constants.Material.ATTRS.FILESIZE.alias).toString());
        try(IStorage iStorage = storageServerFactor.getStorageServer();){
            while(redata.get()<=fileSize) {
                if (updeadtime.get() < 0) {
                    break;
                }
                Thread.sleep(1000);
                redata.set(iStorage.uploadedSize(space, materials.get(0).getNodeCode()));
                //如果已经等于了 那么就直接跳出
                if (redata.get() == fileSize) {
                    //并且删除 缓存在内存的种的对应的 数据
                    iMaterialService.updateMaterialState(materials.get(0).getNodeCode(),Constants.Material.NORMAL);
                    boolean flag= iStorage.merge(space,materials.get(0).getNodeCode(),materials.get(0).getNodeName());
                    if(!flag){
                        throw  new RuntimeException("合并文件失败");
                    }
                    messagingTemplate.convertAndSend(Constants.WebSocket.QUEUE + "/process/" + guid, new ResponseMessage(redata));
                    break;
                }
                messagingTemplate.convertAndSend(Constants.WebSocket.QUEUE + "/process/" + guid, new ResponseMessage(redata));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
