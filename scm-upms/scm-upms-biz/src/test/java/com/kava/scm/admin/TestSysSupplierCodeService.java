package com.kava.scm.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kava.scm.admin.api.entity.SysUser;
import com.kava.scm.admin.service.SysSupplierCodeService;
import com.kava.scm.admin.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
@Slf4j
public class TestSysSupplierCodeService {
	@Autowired
	private SysSupplierCodeService sysSupplierCodeService;
	@Autowired
	private SysUserService sysUserService;

	@Test
	public void testSupplierCodeList() {
		log.info("开始执行testGetList测试方法...");
		System.out.println("开始执行testGetList测试方法...");
		sysSupplierCodeService.getSupplierCodeList("1");
	}

	@Test
	public void testGetList() {
		System.out.println("开始执行testGetList测试方法...");
		final List<SysUser> list = sysUserService.list();
		System.out.println("............." + list);

	}
}
