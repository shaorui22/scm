package com.kava.scm.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/weChat")
@RequiredArgsConstructor
public class WeChatLoginController {
	private static final String APP_ID = "wxfaee2d1ea410c380";
	private static final String APP_SECRET = "17032b05120164f1264b27f7f7971bcc";
	private static final String JS_CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@PostMapping("/login")
	@SneakyThrows
	public Map<String, Object> login(@RequestBody Map<String, String> body) throws IOException {
		String code = body.get("code");
		String nickName = body.get("nickName");
		String inviteCode = body.get("inviteCode");

		if (code == null || code.isEmpty()) {
			return Map.of("status", "error", "message", "code不能为空");
		}
		log.info("code: {}", code);

		// 1. 调用微信接口获取openid和session_key
		String url = JS_CODE2SESSION_URL + "?appid=" + APP_ID
				+ "&secret=" + APP_SECRET
				+ "&js_code=" + code
				+ "&grant_type=authorization_code";

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(url);
			String response = client.execute(request, httpResponse -> EntityUtils.toString(httpResponse.getEntity()));

			JsonNode jsonNode = objectMapper.readTree(response);
			log.info("response。。。。。。。。。。。。。: {}", response);

			if (jsonNode.has("errcode")) {
				return Map.of("status", "error", "message", jsonNode.get("errmsg").asText());
			}

			String openid = jsonNode.get("openid").asText();
			String sessionKey = jsonNode.get("session_key").asText();

			// 查询openId是否已经存在


			// 2. 这里可以做自己的用户逻辑，比如根据 openid 查库，生成 sessionId
			String sessionId = generateSessionId(openid);

			// 3. 模拟用户存储，可以根据需要保存昵称、邀请码等
			// saveOrUpdateUser(openid, nickName, inviteCode);

			Map<String, Object> result = new HashMap<>();
			result.put("status", "success");
			result.put("sessionId", sessionId);
			result.put("openid", openid);
			return result;
		}
	}

	// 生成 sessionId（示例）
	private String generateSessionId(String openid) {
		return "SESSION_" + openid + "_" + System.currentTimeMillis();
	}
}
