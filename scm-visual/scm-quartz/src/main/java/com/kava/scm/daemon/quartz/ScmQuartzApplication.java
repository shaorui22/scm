package com.kava.scm.daemon.quartz;

import com.kava.scm.common.feign.annotation.EnableScmFeignClients;
import com.kava.scm.common.security.annotation.EnableScmResourceServer;
import com.kava.scm.common.swagger.annotation.EnableScmDoc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author frwcloud
 * @date 2023-07-05
 */
@EnableScmDoc("job")
@EnableScmFeignClients
@EnableScmResourceServer
@EnableDiscoveryClient
@SpringBootApplication
public class ScmQuartzApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScmQuartzApplication.class, args);
	}

}
