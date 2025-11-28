package com.kava.scm.common.security.service;

import com.kava.scm.admin.api.dto.UserDTO;
import com.kava.scm.admin.api.dto.UserInfo;
import com.kava.scm.admin.api.feign.RemoteUserService;
import com.kava.scm.common.core.constant.CacheConstants;
import com.kava.scm.common.core.constant.SecurityConstants;
import com.kava.scm.common.core.util.R;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@RequiredArgsConstructor
public class ScmEmailUserDetailsServiceImpl implements ScmUserDetailsService {

	private final RemoteUserService remoteUserService;

	private final CacheManager cacheManager;

	/**
	 * 邮箱登录
	 * @param email, 邮箱
	 * @return
	 */
	@Override
	@SneakyThrows
	public UserDetails loadUserByUsername(String email) {
		Cache cache = cacheManager.getCache(CacheConstants.USER_DETAILS);
		if (cache != null && cache.get(email) != null) {
			return (ScmUser) cache.get(email).get();
		}

		UserDTO userDTO = new UserDTO();
		userDTO.setEmail(email);
		R<UserInfo> result = remoteUserService.info(userDTO);

		UserDetails userDetails = getUserDetails(result);
		if (cache != null) {
			cache.put(email, userDetails);
		}
		return userDetails;
	}

	/**
	 * check-token 使用
	 * @param scmUser user
	 * @return
	 */
	@Override
	public UserDetails loadUserByUser(ScmUser scmUser) {
		return this.loadUserByUsername(scmUser.getEmail());
	}

	/**
	 * 是否支持此客户端校验
	 * @param clientId 目标客户端
	 * @return true/false
	 */
	@Override
	public boolean support(String clientId, String grantType) {
		return SecurityConstants.EMAIL.equals(grantType);
	}

}
