package com.kava.scm.auth.support.weChat;

import com.kava.scm.admin.api.feign.RemoteUserService;
import com.kava.scm.common.core.constant.CommonConstants;
import com.kava.scm.common.core.constant.SecurityConstants;
import com.kava.scm.common.core.util.R;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class OAuth2SocialAuthenticationHelper {
	private static final Logger logger = LogManager.getLogger(OAuth2SocialAuthenticationHelper.class);

	@Autowired
	private RemoteUserService remoteUserService;
	String findUserNameByWxOpenid(String wxOpenid) {
		R<String> result = remoteUserService.getUserNameByWxOpenid(wxOpenid, SecurityConstants.FROM_IN);
		if (result.getCode() == CommonConstants.SUCCESS) {
			String name = result.getData();
			logger.info("find user name:" + name);
			return name;
		} else {
			throw new UsernameNotFoundException("用户不存在");
		}
	}

}
