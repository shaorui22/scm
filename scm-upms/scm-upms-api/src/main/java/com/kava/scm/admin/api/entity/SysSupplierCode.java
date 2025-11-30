package com.kava.scm.admin.api.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema(description = "供应商邀请码")
@EqualsAndHashCode(callSuper = true)
public class SysSupplierCode extends Model<SysSupplierCode> {
	private static final long serialVersionUID = 1L;
	@Schema(description = "主键Id")
	private Integer id;

	@Schema(description = "供应商id")
	private Integer supplierId;

	@Schema(description = "邀请码")
	private String inviteCode;

	@Schema(description = "删除标记")
	private Boolean delFlag;

}
