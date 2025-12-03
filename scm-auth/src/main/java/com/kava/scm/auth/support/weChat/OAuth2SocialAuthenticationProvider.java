package com.kava.scm.auth.support.weChat;

import com.kava.scm.common.core.constant.SecurityConstants;
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

import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
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
		OAuth2SocialAuthenticationToken weChatAuthToken =
				(OAuth2SocialAuthenticationToken) authentication;

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
		// 根据openid查询用户名称
		// 根据用户名查询用户信息，构建authorization

		String userName = authHelper.findUserNameByWxOpenid(openid);

		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
				new UsernamePasswordAuthenticationToken(userName, null);

		Authentication usernamePasswordAuthentication = authenticationManager
				.authenticate(usernamePasswordAuthenticationToken);

		// @formatter:off
			DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
					.registeredClient(registeredClient)
					.principal(usernamePasswordAuthentication)
					.authorizationServerContext(AuthorizationServerContextHolder.getContext())
					.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // 或你的自定义 grant
					.authorizationGrant(weChatAuthToken)
					.authorizedScopes(registeredClient.getScopes()); // 设置作用域

			// @formatter:on
		OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization
				.withRegisteredClient(registeredClient)
				.principalName(usernamePasswordAuthentication.getName())
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE); // TODO social

		// ----- Access token -----
		OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
		logger.info("tokenContext: {}", tokenContext);
		OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
		if (generatedAccessToken == null) {
			OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
					"The token generator failed to generate the access token.", ERROR_URI);
			throw new OAuth2AuthenticationException(error);
		}
		OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
				generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
				generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
		if (generatedAccessToken instanceof ClaimAccessor) {
			authorizationBuilder
					.id(accessToken.getTokenValue())
					.token(accessToken, (metadata) ->
							metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()))
					.attribute(Principal.class.getName(), usernamePasswordAuthentication);
		} else {
			authorizationBuilder.id(accessToken.getTokenValue()).accessToken(accessToken);
		}

		// ----- Refresh token -----
		OAuth2RefreshToken refreshToken = null;
		if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN) &&
				// Do not issue refresh token to public client
				!clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {

			tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
			OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
			if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
				OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
						"The token generator failed to generate the refresh token.", ERROR_URI);
				throw new OAuth2AuthenticationException(error);
			}
			refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
			authorizationBuilder.refreshToken(refreshToken);
		}

		OAuth2Authorization authorization = authorizationBuilder.build();
		this.authorizationService.save(authorization);

		return new OAuth2AccessTokenAuthenticationToken(
				registeredClient, clientPrincipal, accessToken, refreshToken,
				Objects.requireNonNull(authorization.getAccessToken().getClaims()));
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return OAuth2SocialAuthenticationToken.class.isAssignableFrom(authentication);
	}

	static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
		OAuth2ClientAuthenticationToken clientPrincipal = null;
		if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
			clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
		}
		if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
			return clientPrincipal;
		}
		throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
	}
}
