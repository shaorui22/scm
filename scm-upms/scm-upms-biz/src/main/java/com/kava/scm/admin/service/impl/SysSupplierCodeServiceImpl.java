package com.kava.scm.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kava.scm.admin.api.dto.SupplierData;
import com.kava.scm.admin.api.entity.SysSupplierCode;
import com.kava.scm.admin.mapper.SysSupplierCodeMapper;
import com.kava.scm.admin.service.SysSupplierCodeService;
import com.kava.scm.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SysSupplierCodeServiceImpl  extends ServiceImpl<SysSupplierCodeMapper, SysSupplierCode>  implements SysSupplierCodeService {

	@Autowired
	private SysSupplierCodeMapper sysSupplierCodeMapper;
	@Override
	public R getSupplierCodeList(String condition) {
		R result = new R();
		try{

			final List<SupplierData> supplierCodeList = sysSupplierCodeMapper.getSupplierCodeList(condition);
			System.out.println("......." + supplierCodeList);

			List<Integer> ids = supplierCodeList.stream().map(SupplierData::getId).toList();
			// 查询供应商邀请码
			List<SysSupplierCode> sysSupplierCodeList = this.listByIds(ids);
			supplierCodeList.forEach(supplierData -> {
				SysSupplierCode item = sysSupplierCodeList.stream().filter(sysSupplierCode -> sysSupplierCode.getId().equals(supplierData.getSupplierId()))
						.findFirst().orElse(null);
				if (item != null) {
					supplierData.setId(item.getId());
					supplierData.setInviteCode(item.getInviteCode());

				}



			});
			result.setData(supplierCodeList);

		} catch (Exception e) {
			result.setCode(-1);
			log.info("查询供应商信息异常{}", e);
			e.printStackTrace();
		}

		return result;
	}
}
