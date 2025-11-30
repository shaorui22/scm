/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the scm4cloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.kava.scm.admin;

import com.kava.scm.common.datasource.annotation.EnableDynamicDataSource;
import com.kava.scm.common.feign.annotation.EnableScmFeignClients;
import com.kava.scm.common.security.annotation.EnableScmResourceServer;
import com.kava.scm.common.swagger.annotation.EnableScmDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author lengleng
 * @date 2018年06月21日
 * <p>
 * 用户统一管理系统
 */
@EnableScmDoc(value = "admin")
@EnableScmFeignClients
@EnableScmResourceServer
@EnableDiscoveryClient
@SpringBootApplication
@EnableDynamicDataSource
public class ScmAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScmAdminApplication.class, args);
	}

}
