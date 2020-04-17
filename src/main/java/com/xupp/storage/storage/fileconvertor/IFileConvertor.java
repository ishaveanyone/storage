

package com.xupp.storage.storage.fileconvertor;

import com.xupp.storage.define.MaterialViewResultVO;

public interface IFileConvertor {
    /**
     *
     * @param downPath 源文件实际地址
     * @param localserverpath 文件本机服务地址
     * @param remoteserverpath 远程服务端地址
     * @return
     */
    MaterialViewResultVO convert(String downPath, String localserverpath, String remoteserverpath);

}
