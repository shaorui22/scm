package com.kava.scm.auth.support.weChat;


import com.kava.scm.common.core.constant.SecurityConstants;
import com.kava.scm.common.security.util.OAuth2EndpointUtils;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.authentication.AuthenticationConverter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;


public class OAuth2SocialAuthenticationConverter implements AuthenticationConverter {
	private RegisteredClientRepository registeredClientRepository = null;

	public OAuth2SocialAuthenticationConverter() {
		this.registeredClientRepository = registeredClientRepository;
	}

	@Override
	public Authentication convert(HttpServletRequest request) {

		String grantType = request.getParameter("grant_type");
		System.out.println("grant_type is: ..."+ grantType);
		if (!SecurityConstants.WECHAT_MP.equals(grantType)) {
			return null;
		}

		String jsCode = request.getParameter("code");
		if (jsCode == null || jsCode.isEmpty()) {
			throw new IllegalArgumentException("js_code is required");
		}


		MultiValueMap<String, String> parameters = OAuth2EndpointUtils.getParameters(request);
		Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
		Map<String, Object> additionalParameters = new HashMap<>();

		parameters.forEach((key, value) -> {
			if (!key.equals(OAuth2ParameterNames.GRANT_TYPE) &&
					!key.equals(OAuth2ParameterNames.CLIENT_ID) &&
					!key.equals(OAuth2ParameterNames.CODE) &&
					!key.equals(OAuth2ParameterNames.REDIRECT_URI)) {
				additionalParameters.put(key, value.get(0));
			}
		});


		return new OAuth2SocialAuthenticationToken(jsCode, clientPrincipal, additionalParameters);
	}
}
