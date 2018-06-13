package com.lebaoxun.security.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lebaoxun.commons.utils.DesUtils;

@Configuration
public class SecurityDesConfig {

	@Value("${security.oauth2.des.openid}")
	private String oauth2OpenidKey;
	
	@Value("${security.oauth2.des.token}")
	private String oauth2TokenKey;
	
	@Bean
	public DesUtils openidDes(){
		return new DesUtils(oauth2OpenidKey);
	}
	
	@Bean
	public DesUtils tokenDes(){
		return new DesUtils(oauth2TokenKey);
	}
}
