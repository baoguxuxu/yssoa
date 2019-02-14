package com.lebaoxun.security.oauth2;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.lebaoxun.commons.beans.BeanFactoryUtils;
import com.lebaoxun.commons.exception.I18nMessageException;
import com.lebaoxun.commons.utils.CodeUtil;
import com.lebaoxun.commons.utils.CommonUtil;
import com.lebaoxun.commons.utils.DesUtils;
import com.lebaoxun.commons.utils.StringUtils;
import com.lebaoxun.commons.utils.UAgentInfo;
import com.lebaoxun.security.oauth2.entity.Oauth2;
import com.lebaoxun.security.oauth2.entity.Oauth2UserLog;
import com.lebaoxun.security.oauth2.entity.Oauth2VisitPath;
import com.lebaoxun.soa.core.redis.IRedisCache;

@Component
public class Oauth2SecuritySubject{
	
	private Logger logger = LoggerFactory.getLogger(Oauth2SecuritySubject.class);
	@Resource
	private Environment env;
	@Resource
	private IRedisCache redisCache;
	
	@Resource
	private DesUtils openidDes;
	
	@Resource
	private DesUtils tokenDes;
	
	@Value("${security.oauth2.expires_in:7200}")
	private Long expires_in;
	
	IOuath2UserService getIOuath2UserService(){
		return (IOuath2UserService)BeanFactoryUtils.getBean("ouath2UserService");
	}
	
	public String getOpenid(String account){
		String openid = null;
		if(account != null){
			try {
				openid = openidDes.encrypt(account);
				//logger.debug("openid={},account={}",openid,account);
				return openid;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return openid;
	}
	
	public Oauth2 refreshToken(HttpServletRequest request,String openid){
		try {
			IOuath2UserService ouath2UserService = getIOuath2UserService();
			String scope = ouath2UserService.getScope();
			String userName = openidDes.decrypt(openid);
			//logger.debug("account={},openid={}",account,openid);
			Long userId = null;
			if((userId = ouath2UserService.findByUsername(userName)) == null){
				throw new I18nMessageException("500");
			}
			
			String userAgent = request.getHeader("User-Oauth2UserBase");  
			String httpAccept = request.getHeader("Accept");
			UAgentInfo detector = new UAgentInfo(userAgent, httpAccept);
			String platformSource = "";
			
			if(detector.isWechat()){
				platformSource = "wechat";
				if(detector.isIphone) platformSource += "_ios";
				if(detector.isAndroid) platformSource += "_android";
			}else if (detector.detectMobileQuick()) {
			    //移动端浏览器
				platformSource = "mobile";
				if(detector.isIphone) platformSource += "_ios";
				if(detector.isAndroid) platformSource += "_android";
			} else {
			    //PC浏览器
				platformSource = "pc";
			}
			platformSource += "_"+userAgent;
			
			long timestamp = -1;//永久登录
			if(expires_in > 0){
				timestamp = System.currentTimeMillis() + (expires_in * 1000);
			}
			Oauth2UserLog loginLog = new Oauth2UserLog();
			loginLog.setUserId(userId);
			loginLog.setCreateTime(new Date());
			loginLog.setHost(CommonUtil.getIp2(request));
			loginLog.setLogType("LOGIN");
			loginLog.setPlatformSource(platformSource);
			loginLog.setTimestamp(timestamp);
			
			//accountService.deleteCacheByAgentid(oub.getUserId());
			String assess_token = tokenDes.encrypt(openid+","+System.currentTimeMillis()+","+CodeUtil.generateString(10));
			//Long expires_in = 2 * 60 * 60l;
			if(redisCache.exists(scope+":oauth2:assess_token:"+openid)){//当前帐号在其他地方登录，需要删除agent值
				loginLog.setLogType("LOGIN_KICK_OUT");//此表示，本次登录有将其他登录踢出
				Object token = redisCache.get(scope+":oauth2:assess_token:"+openid);
				if(token != null && token instanceof Oauth2){
					Oauth2 oauth2 = (Oauth2)token;
					if((oauth2.getTimestamp() == -1 || oauth2.getTimestamp() > System.currentTimeMillis())){
						Object obj = redisCache.get(scope+":oauth2:user:"+oauth2.getAssess_token());
						if(obj != null && obj instanceof Oauth2UserLog){
							/*loginLog1.setLogType("LOGOUT_KICK_OUT");
							loginLog1.setPlatformSource(log.getPlatformSource());
							loginLog1.setDescr("在其他地方登录，被登出");
							loginLog1.setAdjunctInfo("KICK_OUT");
							agentLogService.createLog(loginLog1);*/
						}
						redisCache.del(scope+":oauth2:user:"+oauth2.getAssess_token());
					}
				}
			}
			
			ouath2UserService.saveLoginLog(loginLog);
			
			Oauth2 oauth2 = new Oauth2();
			oauth2.setAssess_token(assess_token);
			oauth2.setOpenid(openid);
			oauth2.setExpires_in(expires_in);
			oauth2.setTimestamp(timestamp);
			
			redisCache.set(scope+":oauth2:assess_token:"+openid, oauth2);
			//logger.debug("Oauth2UserBase.getAgentId={}",Oauth2UserBase.getAgentId());
			redisCache.set(scope+":oauth2:user:"+assess_token, loginLog);
			
			return oauth2;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateLog(String accessToken,HttpServletRequest request){
		IOuath2UserService ouath2UserService = getIOuath2UserService();
		String userAgent = request.getHeader("User-Oauth2UserBase");  
		String httpAccept = request.getHeader("Accept");
		UAgentInfo detector = new UAgentInfo(userAgent, httpAccept);
		String platformSource = "";
		if(detector.isWechat()){
			platformSource = "wechat";
			if(detector.isIphone) platformSource += "_ios";
			if(detector.isAndroid) platformSource += "_android";
		}else if (detector.detectMobileQuick()) {
		    //移动端浏览器
			platformSource = "mobile";
			if(detector.isIphone) platformSource += "_ios";
			if(detector.isAndroid) platformSource += "_android";
		} else {
		    //PC浏览器
			platformSource = "pc";
		}
		platformSource += "_"+userAgent;
		String scope = ouath2UserService.getScope();
		String key = scope+":oauth2:user:"+accessToken;
		Object obj = redisCache.get(key);
		if(obj != null && obj instanceof Oauth2UserLog){
			Oauth2UserLog log = (Oauth2UserLog)obj;
			int isUpdate = 0;
			if(!platformSource.equals(log.getPlatformSource())){
				log.setPlatformSource(platformSource);
				isUpdate ++;
			}
			String host = CommonUtil.getIp2(request);
			if(!host.equals(log.getHost())){
				log.setHost(host);
				isUpdate ++;
			}
			logger.debug("isUpdate={},hostIp={},log.getHostIp={}",isUpdate,host,log.getHost());
			if(isUpdate > 0){
				redisCache.update(key, obj, null);
			}
		}
	}
	
	public boolean checkToken(String assess_token){
		try {
			IOuath2UserService ouath2UserService = getIOuath2UserService();
			String scope = ouath2UserService.getScope();
			String openid = getOpenidByToken(assess_token);
			//logger.debug("openid={},assess_token={}",openid,assess_token);
			boolean result = false;
			if(redisCache.exists(scope+":oauth2:assess_token:"+openid)){
				Object obj = (Object)redisCache.get(scope+":oauth2:assess_token:"+openid);
				if(obj != null && obj instanceof Oauth2){
					Oauth2 oauth2 = (Oauth2)obj;
					if(assess_token.equals(oauth2.getAssess_token())
							&& (oauth2.getTimestamp() == -1 || oauth2.getTimestamp() > System.currentTimeMillis())){
						result = true;
					}
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private String getOpenidByToken(String assess_token) throws Exception{
		//logger.debug("getOpenidByToken|assess_token={}",assess_token);
		String str = tokenDes.decrypt(assess_token);
		if(StringUtils.isBlank(str) || str.indexOf(",") < 0){
			throw new I18nMessageException("500");
		}
		String openid = str.split(",")[0];
		return openid;
	}
	
	public Long getCurrentUser() {
		try {
			return getCurrentUserLog().getUserId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Oauth2UserLog getCurrentUserLog() {
		IOuath2UserService ouath2UserService = getIOuath2UserService();
		String assess_token = Oauth2AccessToken.getToken();
		try {
			String scope = ouath2UserService.getScope();
			return ((Oauth2UserLog)redisCache.get(scope+":oauth2:user:"+assess_token));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void logout() {
		IOuath2UserService ouath2UserService = getIOuath2UserService();
		String scope = ouath2UserService.getScope();
		String assess_token = Oauth2AccessToken.getToken();
		String openid;
		logger.debug("assess_token={}",assess_token);
		try {
			if(StringUtils.isNotBlank(assess_token)){
				openid = getOpenidByToken(assess_token);
				String key = scope+":oauth2:assess_token:"+openid;
				if(redisCache.exists(key)){
					Object obj = redisCache.get(scope+":oauth2:user:"+assess_token);
					if(obj != null && obj instanceof Oauth2UserLog){
						Oauth2UserLog log = (Oauth2UserLog)obj;
						Oauth2UserLog loginLog = new Oauth2UserLog();
						loginLog.setUserId(log.getUserId());
						loginLog.setCreateTime(new Date());
						loginLog.setHost(log.getHost());
						loginLog.setLogType("LOGOUT");
						loginLog.setPlatformSource(log.getPlatformSource());
						ouath2UserService.saveLoginLog(loginLog);
					}
					redisCache.del(key);
					redisCache.del(scope+":oauth2:user:"+assess_token);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean check(Long userId,String path){
		String names = env.getProperty("oauth2.access.whites.names");
    	if(StringUtils.isNotBlank(names)){
    		String nms[] = names.split("\\,");
    		for(String nm : nms){
    			logger.debug("path={},nm={}",path,nm);
    			String sta = "oauth2.access.whites."+nm+".";
    			String pathPattern = env.getProperty(sta+"pathPattern");
    			if(StringUtils.isBlank(pathPattern)){
    				continue;
    			}
    			String pps[] = pathPattern.split(",");
    			for(String pp : pps){
    				//logger.debug("pp={},path={},matches={}",pp,path,pp.matches(path));
    				if(path.matches(pp)
    						&& !Arrays.asList(env.getProperty(sta+"whiteList").split(","))
    						.contains(userId)){
    					return false;
    				}
    			}
    		}
    	}
    	return true;
	}
	
	public boolean isWhiteAccess(Long userId,String path){
		IOuath2UserService ouath2UserService = getIOuath2UserService();
		List<Oauth2VisitPath> paths = ouath2UserService.findWhiteAccess();
		if(paths == null || paths.isEmpty()){
			return check(userId, path);
		}
		for(Oauth2VisitPath avp : paths){
			if(avp == null || StringUtils.isBlank(avp.getPath())){
				continue;
			}
			String pp = avp.getPath();
			logger.debug("pp={},path={},matches={}",pp,path,pp.matches(path));
			if(path.matches(pp)
					&& !avp.getWhites().contains(userId)){
				return false;
			}
		}
    	return true;
	}
	
	public Oauth2 getCurrentOauth2(String account,String scope){
		if(StringUtils.isNotBlank(account)){
			String openid;
			try {
				openid = openidDes.encrypt(account);
				logger.debug("openid={}",openid);
				String openidKey = scope+":oauth2:assess_token:"+openid;
				if(redisCache.exists(openidKey)){
					Object token = (String)redisCache.get(openidKey);
					if(token != null && token instanceof Oauth2){
						Oauth2 oauth2 = (Oauth2)token;
						if((oauth2.getTimestamp() == -1 || oauth2.getTimestamp() > System.currentTimeMillis())){
							return oauth2;
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public Oauth2UserLog getSessionUser(String account,String scope){
		if(StringUtils.isNotBlank(account)){
			try {
				Oauth2 oauth2 = getCurrentOauth2(account, scope);
				if(oauth2 != null){
					return (Oauth2UserLog)redisCache.get(scope+":oauth2:user:"+oauth2.getAssess_token());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
