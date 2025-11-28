package com.kava.scm.codegen.config;

import cn.smallbun.screw.core.constant.DefaultConstants;
import lombok.Data;
import org.anyline.util.ConfigTable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 代码生成默认配置类
 *
 * @author lengleng
 * @date 2024/7/15
 */
@Data
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = ScmCodeGenDefaultProperties.PREFIX)
public class ScmCodeGenDefaultProperties implements InitializingBean {

	public static final String PREFIX = "codegen";

	/**
	 * 是否开启在线更新
	 */
	private boolean autoCheckVersion = true;

	/**
	 * 模板项目地址
	 */
	private String onlineUrl = DefaultConstants.CGTM_URL;

	/**
	 * 生成代码的包名
	 */
	private String packageName = "com.kava.scm";

	/**
	 * 生成代码的版本
	 */
	private String version = "1.0.0";

	/**
	 * 生成代码的模块名
	 */
	private String moduleName = "admin";

	/**
	 * 生成代码的后端路径
	 */
	private String backendPath = "scm";

	/**
	 * 生成代码的前端路径
	 */
	private String frontendPath = "scm-ui";

	/**
	 * 生成代码的作者
	 */
	private String author = "scm";

	/**
	 * 生成代码的邮箱
	 */
	private String email = "sw@scmx.vip";

	/**
	 * 表单布局（一列、两列）
	 */
	private Integer formLayout = 2;

	/**
	 * 下载方式 （0 文件下载、1写入目录）
	 */
	private String generatorType = "0";

	@Override
	public void afterPropertiesSet() throws Exception {
		ConfigTable.KEEP_ADAPTER = 0;
	}

}
