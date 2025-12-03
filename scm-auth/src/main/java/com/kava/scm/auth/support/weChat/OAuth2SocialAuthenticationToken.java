package com.kava.scm.auth.support.weChat;


import com.kava.scm.common.core.constant.SecurityConstants;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @description
 */
public class OAuth2SocialAuthenticationToken extends AbstractAuthenticationToken {

	private final String jsCode;
	private final Authentication clientPrincipal;
	private final Map<String, Object> additionalParameters;

	/**
	 *
	 * @param clientPrincipal the authenticated client principal
	 * @param additionalParameters the additional parameters
	 */
	protected OAuth2SocialAuthenticationToken(
			String jsCode,
			Authentication clientPrincipal,
			@Nullable Map<String, Object> additionalParameters) {
		super(Collections.emptyList());
		Assert.notNull(clientPrincipal, "clientPrincipal cannot be null");
		Assert.notNull(jsCode, "jsCode cannot be null");

		this.jsCode = jsCode;
		this.clientPrincipal = clientPrincipal;
		this.additionalParameters = Collections.unmodifiableMap(
				additionalParameters != null ? new HashMap<>(additionalParameters) : Collections.emptyMap());
	}

	public AuthorizationGrantType getGrantType() {
		return AuthorizationGrantType.AUTHORIZATION_CODE;
	}

	// 用户名
	@Override
	public Object getPrincipal() {
		return this.clientPrincipal;
	}

	// 不需要密码
	@Override
	public Object getCredentials() {
		return "";
	}

	public Map<String, Object> getAdditionalParameters() {
		return this.additionalParameters;
	}

	public String getJsCode() {
		return this.jsCode;
	}

}
