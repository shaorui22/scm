package com.kava.scm.auth.support.weChat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.mp")
public class WeChatProperties {
	private String appId;
	private String secret;
}
