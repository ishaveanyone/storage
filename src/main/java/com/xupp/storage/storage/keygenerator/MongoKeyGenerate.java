/**
 * Company: 上海数慧系统技术有限公司
 * Department: 数据中心
 * Date: 2020-04-16 15:09
 * Author: xupp
 * Email: xupp@dist.com.cn
 * Desc：
 */

package com.xupp.storage.storage.keygenerator;

import org.bson.BsonObjectId;

public class MongoKeyGenerate implements IKeyGenerate{
     private static MongoKeyGenerate mongoKeyGenerate=new MongoKeyGenerate();
     public static MongoKeyGenerate getInstance(){
         return  mongoKeyGenerate;
     }

    /**
     * 获取一个 主键
     * @return
     */
    public BsonObjectId generateKey(){
        return new BsonObjectId();
    }


}
