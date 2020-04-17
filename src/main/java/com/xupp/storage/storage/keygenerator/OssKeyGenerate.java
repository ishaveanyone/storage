/**
 * Company: 上海数慧系统技术有限公司
 * Department: 数据中心
 * Date: 2020-04-16 15:10
 * Author: xupp
 * Email: xupp@dist.com.cn
 * Desc：
 */

package com.xupp.storage.storage.keygenerator;

import java.util.UUID;

public class OssKeyGenerate implements IKeyGenerate{

    private static OssKeyGenerate ossKeyGenerate=new OssKeyGenerate();
    public static OssKeyGenerate getInstance(){
        return  ossKeyGenerate;
    }

    /**
     * 获取一个 主键
     * @return
     */
    public String generateKey(){
        return  UUID.randomUUID().toString().replaceAll("-","");
    }


}
