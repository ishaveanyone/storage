package com.xupp.storage.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 配合 文档预览的参数
 */
@Setter
@Getter
public class MaterialViewResultVO implements Serializable {


	/**
	 * 文件地址
	 * 如果本地 则是 本地的服务 url
	 * 如果三方 就是 需要的地址 url
	 */
	private String path;

	/**
	 * 添加文件下载说明 如果 是 pdf jpg此类文件 那么不需要进行转就可以直接返回
	 */
	private boolean isDownLoad;

	public MaterialViewResultVO(String path, boolean isDownLoad) {
		this.path = path;
		this.isDownLoad = isDownLoad;
	}
}
