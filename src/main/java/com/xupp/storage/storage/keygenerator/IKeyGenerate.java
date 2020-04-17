/**
 * Company: 上海数慧系统技术有限公司
 * Department: 数据中心
 * Date: 2019-12-19 11:25
 * Author: xupp
 * Email: xupp@dist.com.cn
 * Desc：
 */
package com.xupp.storage.storage.keygenerator;

/**
 * 主键生成策略  不同服务端 存储的时候 唯一的标识 可能不太一样
 * oss：
     * 文件的命名规则如下：
     * 使用UTF-8编码。
     * 长度必须在1–1023字节之间。
     * 不能以正斜线（/）或者反斜线（\）字符开头。
 * mongo：
 * 使用 mongo 自带的 objectid进行数据的生成
 */
public interface IKeyGenerate {



}
