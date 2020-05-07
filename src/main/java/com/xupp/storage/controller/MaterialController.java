package com.xupp.storage.controller;



import com.alibaba.fastjson.JSONObject;
import com.xupp.storage.define.*;
import com.xupp.storage.facade.MaterialFacade;
import com.xupp.storage.model.dto.MaterialQueryDTO;
import com.xupp.storage.model.vo.MaterialViewResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value="rest/material")
@Api(tags = "API-文件服务存储")
public class MaterialController {
    @Autowired
    private MaterialFacade materialFacade;

    @PostMapping(value="private/v1/dir")
    @ApiOperation(value="文件夹保存",
            response = Object.class,
            responseContainer = "ResponseData")
    public Object addMaterialDir(
            HttpServletRequest httpServletRequest,
            @RequestBody MaterialRequest addRequest)  {
        if(addRequest.getParentGuid()==null||
                "".equals(addRequest.getParentGuid())){
            addRequest.setParentGuid(String.valueOf(Constants.Material.MATERIAL_ROOT_DIR_PID));
        }
        return  this.materialFacade.addMaterial(addRequest,MaterialTypeEnum.DIRECTORY,httpServletRequest);
    }


    @PostMapping(value="private/v1/file")
    @ApiOperation(value="文件保存(可以绑定参数file 保存一个小文件)",
            response = Object.class,
            responseContainer = "ResponseData")
    public Object addMaterialFile(
             HttpServletRequest httpServletRequest,
             String refId,
             String name,
             String parentGuid,
             String space,
             String tags,
             long fileSize)  {
        MaterialRequest materialRequest=new MaterialRequest();
        materialRequest.setParentGuid(parentGuid);
        materialRequest.setRefId(refId);
        materialRequest.setName(name);
        materialRequest.setSpace(space);
        Map map=  JSONObject.parseObject(tags,Map.class);
        map.put(Constants.Material.ATTRS.FILESIZE.alias,String.valueOf(fileSize));
        materialRequest.setTags(map);
        return  this.materialFacade.addMaterial(materialRequest, MaterialTypeEnum.FILE,httpServletRequest);
    }



    @DeleteMapping(value="private/v1/material/{space}")
    @ApiOperation(value="批量删除",
            response = Boolean.class,
            responseContainer = "ResponseData")
    public Object deleteMaterial(
            @PathVariable String space,
            @RequestBody  List<String> guids
            ){
        return  this.materialFacade.deleteMaterial(space,guids);
    }

    //预览文件
    @GetMapping(value="private/v1/preview/{key}/{space}")
    @ApiOperation(value="预览文件",
            response = MaterialViewResultVO.class,
            responseContainer = "ResponseData")
    public Object previewMaterial(
        @PathVariable String key,
        @PathVariable String space,
        HttpServletRequest httpServletRequest
        ) throws IOException {
        return this.materialFacade.previewMaterial(space,key,httpServletRequest);
    }

    //
    @GetMapping(value="private/v1/download/{key}/{space}")
    @ApiOperation(value="下载文件",
            response = MaterialViewResultVO.class,
            responseContainer = "ResponseData")
    public Object downloadMaterial(
            @PathVariable String key,
            @PathVariable String space,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse

    ) throws IOException {
        MaterialViewResultVO materialViewResultVO=
                this.materialFacade.downloadMaterial(space,key,httpServletRequest);
        String filePath= materialViewResultVO.getPath();
        String fileName=filePath.substring(filePath.lastIndexOf(File.separator)+1);
        File file=new File(filePath);
        if(file.exists()){ //判断文件父目录是否存在
            httpServletResponse.setContentType("application/vnd.ms-excel;charset=UTF-8");
            httpServletResponse.setCharacterEncoding("UTF-8");
            // response.setContentType("application/force-download");
            httpServletResponse.setHeader("Content-Disposition", "attachment;fileName=" +   java.net.URLEncoder.encode(fileName,"UTF-8"));
            RandomAccessFile randomAccessFile=null;
            OutputStream os = null; //输出流
            try {
                //使用nio 可以及时清理 byte数组 不用新建
                randomAccessFile=new RandomAccessFile(file.getAbsolutePath(),"rw");
                FileChannel fc = randomAccessFile.getChannel();
                ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
                os = httpServletResponse.getOutputStream();
                while ((fc.read(byteBuffer)) >= 0) {
                    //翻转指针
                    byteBuffer.flip();
                    //remaining = limit - position
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    os.write(bytes);
                    byteBuffer.clear();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                randomAccessFile.close();
                file.delete();//最后一步删除临时文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //分级获取资料数据
    @PostMapping(value="private/v1/query")
    @ApiOperation(value="搜索文件 如果获取第一级那么就使用 pid =-1",
            response = Object.class,
            responseContainer = "ResponseData")
    public Object getMaterialByLevel(
            @RequestBody MaterialQueryDTO materialQueryDTO
            ){
        return this.materialFacade.getMaterialByLevel(materialQueryDTO);
    }

    //断点续传
    @PostMapping(value="private/v1/checkUploaded")
    @ApiOperation(value="使用轮询返回后台对应文件的已经缓存大小",
            response = String.class,
            responseContainer = "ResponseData")
    public Object checkUploaded(
            @RequestBody  MaterialQueryDTO materialQueryDTO){
        return this.materialFacade.checkUploaded(materialQueryDTO);
    }

    //断点续传
    @PostMapping(value="private/v1/websocketCheckUploaded/{guid}")
    @ApiOperation(value="使用websocket返回后台对应文件的已经缓存大小",
            response = Long.class,
            responseContainer = "ResponseData")
    public Object websocketCheckUploaded(
            @PathVariable String guid,
            @RequestBody MaterialQueryDTO materialQueryDTO) throws InterruptedException {
        materialFacade.websocketCheckUploaded(guid,materialQueryDTO);
        return materialFacade.checkUploaded(materialQueryDTO);
    }

    @PostMapping(value="private/v1/part/file")
    @ApiOperation(value="上传分片文件",
            response = Object.class,
            responseContainer = "ResponseData")
    public Object addMaterialPartFile(
            HttpServletRequest httpServletRequest,
            String space,//存储的空间域
            Integer chunkNum,//分片编号
            String nodeGuid //文件key
           ) {
        return materialFacade.addMaterialPartFile(
                 httpServletRequest,
                 space,//存储的空间域
                 chunkNum,//分片编号
                 nodeGuid);
    }





}
