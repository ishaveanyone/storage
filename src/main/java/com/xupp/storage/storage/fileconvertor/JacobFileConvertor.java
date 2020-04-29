/**
 * Company: 上海数慧系统技术有限公司
 * Department: 数据中心
 * Date: 2020-03-29 14:16
 * Author: xupp
 * Email: xupp@dist.com.cn
 * Desc：
 */

package com.xupp.storage.storage.fileconvertor;

import com.xupp.storage.define.*;
import com.xupp.storage.define.util.OfficeToPdf;
import com.xupp.storage.define.util.StringUtil;
import com.xupp.storage.define.util.WpsToPdf;
import com.xupp.storage.model.vo.MaterialViewResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 使用jacob 进行文件转换的功能类
 */
@Component
public class JacobFileConvertor implements IFileConvertor{
    private Logger log= LoggerFactory.getLogger(JacobFileConvertor.class);
    @Value("${storage.convertor.environment}")
    private String environment;

    /**
     * 值得注意的是 这个pdf下载的位置 上一级目录对应 给定源文件的 目录
     * @param downPath 源文件下载地址
     * @return 返回对应的是 ser
     */
    public MaterialViewResultVO convert(String downPath, String localserverpath, String remoteserverpath){
        //pdf 文件路径
        String suffix= StringUtil.catLastStrByChar(downPath, '.').toLowerCase();
        String pdfPath= StringUtil.
                catBeforeStrByChar(downPath, '.')+".pdf";
        //下载文件的路径
        if(Constants.Material.NO_NEED_TRANSFORM_FILTER.contains(suffix.toLowerCase())) {
            return new MaterialViewResultVO(localserverpath,false);
        }else if (Constants.Material.NEED_DOWNLOAD_FILTER.contains(suffix.toLowerCase())) {
            return new MaterialViewResultVO(localserverpath,true);
        }else if(Constants.Material.NEED_TRANSFORM_CHARSET_FILTER.contains(suffix.toLowerCase())) {
            // 返回转码(UTF-8)后的文件,如果已经存在就不进行
            if(new File(pdfPath).exists()) {
                return new MaterialViewResultVO(StringUtil.
                        catBeforeStrByChar(localserverpath, '.')+".pdf" ,
                        false);
            }
            if("WPS".equalsIgnoreCase(environment)){
                if (WpsToPdf.convert2PDF(downPath, pdfPath)) {
                    log.info(downPath+">>转化pdf成功>>" + pdfPath);
                    return new MaterialViewResultVO(StringUtil.
                            catBeforeStrByChar(localserverpath, '.')+".pdf" ,
                            false);
                } else {
                    log.info(downPath+">>转化pdf失败>>" + pdfPath);
                    throw new RuntimeException("转化pdf失败");
                }
            }else {
                if (OfficeToPdf.convert2PDF(downPath, pdfPath)) {
                    log.info(downPath+">>转化pdf成功>>" + pdfPath);
                    return new MaterialViewResultVO(StringUtil.
                            catBeforeStrByChar(localserverpath, '.')+".pdf" ,false);
                } else {
                    log.info(downPath+">>转化pdf失败>>" + pdfPath);
                    throw new RuntimeException(downPath);

                }
            }
        }else {
            throw new RuntimeException(downPath);
        }
    }

}
