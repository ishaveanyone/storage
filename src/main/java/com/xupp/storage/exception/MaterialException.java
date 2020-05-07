/**
 * @date 2019/12/17
 * @author dingchw
 * @email dingchw@dist.com.cn
 * @desc
 */
package com.xupp.storage.exception;


import com.xupp.storage.define.HttpStatusEnum;

public class MaterialException {
    public static class NotFound extends AbstractException {
        public NotFound(String guid){
            super("附件节点【" + guid + "】不存在", HttpStatusEnum.NOTFOUND);
        }
    }

    //文件上传 文件服务器失败
    public static class FileUploadFail extends AbstractException {
        public FileUploadFail(String guid){
            super("附件节点【" + guid + "】上传失败", HttpStatusEnum.ERROR);
        }
    }

    //文件转换格式失败
    public static class FileTransformFail extends AbstractException {
        public FileTransformFail(String guid){
            super("附件节点【" + guid + "】转换格式失败", HttpStatusEnum.ERROR);
        }
    }


    //文件转换格式失败
    public static class FileTransformUnknownFormat extends AbstractException {
        public FileTransformUnknownFormat(String guid){
            super("附件节点【" + guid + "】转换未知格式", HttpStatusEnum.ERROR);
        }
    }

    //删除文件失败
    public static class FileDeleteFail extends AbstractException {
        public FileDeleteFail(String guids){
            super("附件节点【" + guids + "】删除失败", HttpStatusEnum.ERROR);
        }
    }

    //重复添加异常
    public static class FileRepeatAddFail extends AbstractException {
        public FileRepeatAddFail(String fileName){
            super("附件节点【" + fileName + "】重复添加异常", HttpStatusEnum.ERROR);
        }
    }

    public static class FileMultiDataFail extends AbstractException {
        public FileMultiDataFail(){
            super("返回文件数据大小不匹配", HttpStatusEnum.ERROR);
        }
    }
}
