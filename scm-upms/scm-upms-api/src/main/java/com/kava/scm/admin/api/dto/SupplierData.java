package com.kava.scm.admin.api.dto;

import com.kava.scm.admin.api.entity.SysSupplierCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "供应商信息查询")
public class SupplierData extends SysSupplierCode {
	private Integer supplierId;
	private String name;
	private String code;
	private String abbrName;
	private String mainBusiness; // 主营业务



}
