package com.kava.scm.auth.support.weChat;

public interface AuthConnector {
	String getAccessToken(String code);
	String getUserId(String code, String accessToken);

	boolean supports(String clientId);
}
