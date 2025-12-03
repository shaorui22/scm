package com.kava.scm.auth.support.weChat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR_URI;

public class OAuth2SocialAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LogManager.getLogger(OAuth2SocialAuthenticationProvider.class);

	public static final AuthorizationGrantType WECHAT_MP = new AuthorizationGrantType("wechat_mp");

	private final OAuth2SocialAuthenticationHelper authHelper;
	private final AuthenticationManager authenticationManager;
	private final OAuth2AuthorizationService authorizationService;
	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
	private final WeChatProperties weChatProperties;

	public OAuth2SocialAuthenticationProvider(
			OAuth2SocialAuthenticationHelper authHelper,
			AuthenticationManager authenticationManager,
			OAuth2AuthorizationService authorizationService,
			OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
			WeChatProperties weChatProperties) {

		Assert.notNull(authHelper, "authHelper cannot be null");
		Assert.notNull(authenticationManager, "authenticationManager cannot be null");
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
		Assert.notNull(weChatProperties, "weChatProperties cannot be null");

		this.authHelper = authHelper;
		this.authenticationManager = authenticationManager;
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;
		this.weChatProperties = weChatProperties;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		OAuth2SocialAuthenticationToken weChatAuthToken = (OAuth2SocialAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal =
				getAuthenticatedClientElseThrowInvalidClient(weChatAuthToken);
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		// 调用微信接口获取 openid
		Map<String, Object> wxResp = WeChatUtil.jscode2session(
				weChatProperties.getAppId(),
				weChatProperties.getSecret(),
				weChatAuthToken.getJsCode()
		);
		logger.info("wxResp: {}", wxResp);

		if (wxResp == null || wxResp.get("openid") == null) {
			throw new OAuth2AuthenticationException("WeChat login failed: " + wxResp);
		}
		String openid = wxResp.get("openid").toString();

		// 根据 openid 查询用户名称
		String userName = authHelper.findUserNameByWxOpenid(openid);

		// 认证用户
		UsernamePasswordAuthenticationToken userAuthToken =
				new UsernamePasswordAuthenticationToken(userName, null);
		Authentication authenticatedUser = authenticationManager.authenticate(userAuthToken);

		// 构建 OAuth2Authorization
		OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization
				.withRegisteredClient(registeredClient)
				.principalName(authenticatedUser.getName())
				.authorizationGrantType(WECHAT_MP);

		// 构建 token context
		DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
				.registeredClient(registeredClient)
				.principal(authenticatedUser)
				.authorizationGrantType(WECHAT_MP)
				.authorizationGrant(weChatAuthToken)
				.authorizedScopes(registeredClient.getScopes());

		// Access token
		OAuth2TokenContext accessTokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
		OAuth2Token generatedAccessToken = tokenGenerator.generate(accessTokenContext);
		if (generatedAccessToken == null) {
			throw new OAuth2AuthenticationException(new OAuth2Error(
					OAuth2ErrorCodes.SERVER_ERROR,
					"Failed to generate access token",
					ERROR_URI
			));
		}

		OAuth2AccessToken accessToken = new OAuth2AccessToken(
				OAuth2AccessToken.TokenType.BEARER,
				generatedAccessToken.getTokenValue(),
				generatedAccessToken.getIssuedAt(),
				generatedAccessToken.getExpiresAt(),
				accessTokenContext.getAuthorizedScopes()
		);

		// 将 access token 加入授权
		authorizationBuilder.id(accessToken.getTokenValue()).accessToken(accessToken);

		// Refresh token
		OAuth2RefreshToken refreshToken = null;
		if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)
				&& !clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {

			OAuth2TokenContext refreshTokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
			OAuth2Token generatedRefreshToken = tokenGenerator.generate(refreshTokenContext);
			if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
				throw new OAuth2AuthenticationException(new OAuth2Error(
						OAuth2ErrorCodes.SERVER_ERROR,
						"Failed to generate refresh token",
						ERROR_URI
				));
			}
			refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
			authorizationBuilder.refreshToken(refreshToken);
		}

		OAuth2Authorization authorization = authorizationBuilder.build();
		authorizationService.save(authorization);

		return new OAuth2AccessTokenAuthenticationToken(
				registeredClient,
				clientPrincipal,
				accessToken,
				refreshToken,
				Objects.requireNonNull(authorization.getAccessToken().getClaims())
		);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuth2SocialAuthenticationToken.class.isAssignableFrom(authentication);
	}

	private static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
		Authentication principal = (Authentication) authentication.getPrincipal();
		if (principal instanceof OAuth2ClientAuthenticationToken clientAuth && clientAuth.isAuthenticated()) {
			return clientAuth;
		}
		throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
	}
}
