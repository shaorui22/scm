package com.kava.scm.auth.support.weChat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WeChatUtil {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@SuppressWarnings("unchecked")
	public static Map<String, Object> jscode2session(String appid, String secret, String jsCode) {
		log.info("jscode2session appid: {}, secret: {}, jsCode: {}", appid, secret, jsCode);

		String url = "https://api.weixin.qq.com/sns/jscode2session"
				+ "?appid=" + appid
				+ "&secret=" + secret
				+ "&js_code=" + jsCode
				+ "&grant_type=authorization_code";

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(url);
			String response = client.execute(request, httpResponse -> EntityUtils.toString(httpResponse.getEntity()));


			JsonNode jsonNode = objectMapper.readTree(response);
			log.info("response。。。。。。。。。。。。。: {}", response);

			if (jsonNode.has("errcode")) {
				return Map.of("status", "error", "message", jsonNode.get("errmsg").asText());
			} else {
				return Map.of("status", "success", "openid", jsonNode.get("openid").asText());
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
