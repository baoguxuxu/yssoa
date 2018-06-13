package com.lebaoxun.security.oauth2;

import java.util.List;

import com.lebaoxun.security.oauth2.entity.Oauth2UserLog;
import com.lebaoxun.security.oauth2.entity.Oauth2VisitPath;

public interface IOuath2UserService {
	
	Long findByUsername(String username);
	
	void saveLoginLog(Oauth2UserLog log);
	
	List<Oauth2VisitPath> findWhiteAccess();
}
