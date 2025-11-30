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

package com.kava.scm.admin.controller;

import com.kava.scm.admin.api.entity.SysDept;
import com.kava.scm.admin.api.vo.DeptExcelVo;
import com.kava.scm.admin.service.SysDeptService;
import com.kava.scm.admin.service.SysSupplierCodeService;
import com.kava.scm.common.core.util.R;
import com.kava.scm.common.log.annotation.SysLog;
import com.kava.scm.common.security.annotation.HasPermission;
import com.kava.scm.common.security.annotation.Inner;
import com.pig4cloud.plugin.excel.annotation.RequestExcel;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/supplier")
@Tag(description = "supplier", name = "部门管理模块")
@Inner(value = false)
public class SysSupplierCodeController {

	private final SysSupplierCodeService sysSupplierCodeService;

	@GetMapping("/{condition}")
	@Inner (value = false)
	public R getById(@PathVariable String condition) {
		return sysSupplierCodeService.getSupplierCodeList( condition);
	}

}
