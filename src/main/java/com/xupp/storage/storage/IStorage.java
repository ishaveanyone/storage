package com.xupp.storage.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Company: 上海数慧系统技术有限公司
 * Department: 数据中心
 * Date: 2019-12-19 11:25
 * Author: xupp
 * Email: xupp@dist.com.cn
 * Desc：
 */
public interface IStorage extends AutoCloseable {
    IStorage connect();

    /**
     * 不同的服务不一样比如ftp和local对应是path，mongo和oss对应是key
     * 上传到默认的空间上面
     * @param targetKey
     * @param file
     */
    @Deprecated
    boolean uploadFile(String targetKey, File file) throws IOException;

    /**
     * 不同的服务不一样比如ftp和local对应是path，mongo和oss对应是key
     *
     * @param targetKey
     * @param space 指定的空间位置 不同的服务不一样比如ftp和local对应是上级的目录位置，mongo ？ oss 对应的是 bucket
     * @param file
     */
    boolean uploadFileToSpace(String space, String targetKey, File file) throws IOException;

    /**
     * 同步的方案
     * 断电续传
     * @throws Throwable
     */

    boolean uploadContinue(String space, Integer chunkNum, String targetKey, File file) throws Throwable;

    /**
     * 异步的方案
     * @param space
     * @param chunkNum
     * @param targetKey
     * @param file
     * @return
     * @throws Throwable
     */
    Future<Boolean> uploadContinueAsyc(String space, Integer chunkNum, String targetKey, File file) throws Throwable;

    /**
     *
     * 普通下载
     * @param targetKey
     * @return
     */
    @Deprecated
    InputStream downloadFile(String targetKey);

    /**
     * 从具体的存储域下载文件
     * @param space
     * @param targetKey
     * @return
     */
    InputStream downloadFileFromSpace(String space, String targetKey);

    /**
     * 断点下载
     * TODO 需要给出下载的文件的起始位置 一级大小 这个 接口需要重新设计 下一个版本实现
     * @param targetKey
     * @param fileName
     * @return
     * @throws Throwable
     */
    InputStream downloadContinue(String targetKey, String fileName) throws Throwable;

    /**
     * 删除文件
     * @throws RuntimeException
     */

    boolean removeFile(String space, String key);

    /**
     * 批量删除文件
     * @throws RuntimeException
     */
    boolean removeFiles(String space, List<String> key);

    /**
     * 获取文件已经上传的大小
     * @throws RuntimeException
     * @return
     */

    Long uploadedSize(String space, String targerKey) throws IOException;


    @Override
    void close() throws RuntimeException;

    /**
     * 进行分片合并
     * 注意不要将 这一步设置成异步 如果设置成异步 有可能文件还没有合并完成
     */
    Boolean merge(String space, String targerKey, String filename);

    /**
     * 重新指定新的存储空间
     */
    static String checkSpaceLegal(String space, String defaultSpace){
        return space==null?defaultSpace:space;
    }



}
