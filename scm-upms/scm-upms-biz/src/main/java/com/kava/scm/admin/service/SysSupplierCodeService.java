package com.kava.scm.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.kava.scm.admin.api.entity.SysSupplierCode;
import com.kava.scm.common.core.util.R;

public interface SysSupplierCodeService extends IService<SysSupplierCode> {

	R getSupplierCodeList(String condition);
}
