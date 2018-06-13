package com.lebaoxun.security.oauth2;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.lebaoxun.commons.exception.I18nMessageException;
import com.lebaoxun.commons.utils.CodeUtil;
import com.lebaoxun.commons.utils.CommonUtil;
import com.lebaoxun.commons.utils.DesUtils;
import com.lebaoxun.commons.utils.UAgentInfo;
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
	private IOuath2UserService ouath2UserService;
	
	@Resource
	private DesUtils openidDes;
	
	@Resource
	private DesUtils tokenDes;
	
	@Value("${security.oauth2.expires_in:7200}")
	private Long expires_in;
	
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
			
			Oauth2UserLog loginLog = new Oauth2UserLog();
			loginLog.setUserId(userId);
			loginLog.setCreateTime(new Date());
			loginLog.setHost(CommonUtil.getIp2(request));
			loginLog.setLogType("LOGIN");
			loginLog.setPlatformSource(platformSource);
			
			//accountService.deleteCacheByAgentid(oub.getUserId());
			String assess_token = tokenDes.encrypt(openid+","+System.currentTimeMillis()+","+CodeUtil.generateString(10));
			//Long expires_in = 2 * 60 * 60l;
			if(redisCache.exists("oauth2:assess_token:"+openid)){//当前帐号在其他地方登录，需要删除agent值
				loginLog.setLogType("LOGIN_KICK_OUT");//此表示，本次登录有将其他登录踢出
				String otherToken = (String)redisCache.get("oauth2:assess_token:"+openid);
				Object obj = redisCache.get("oauth2:user:"+otherToken);
				
				if(obj != null && obj instanceof Oauth2UserLog){
					/*loginLog1.setLogType("LOGOUT_KICK_OUT");
					loginLog1.setPlatformSource(log.getPlatformSource());
					loginLog1.setDescr("在其他地方登录，被登出");
					loginLog1.setAdjunctInfo("KICK_OUT");
					agentLogService.createLog(loginLog1);*/
				}
				redisCache.del("oauth2:user:"+otherToken);
			}
			
			ouath2UserService.saveLoginLog(loginLog);
			
			redisCache.set("oauth2:assess_token:"+openid, assess_token, expires_in);
			//logger.debug("Oauth2UserBase.getAgentId={}",Oauth2UserBase.getAgentId());
			redisCache.set("oauth2:user:"+assess_token, loginLog ,expires_in+10);
			Oauth2 oauth2 = new Oauth2();
			oauth2.setAssess_token(assess_token);
			oauth2.setOpenid(openid);
			oauth2.setExpires_in(expires_in);
			
			return oauth2;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void updateLog(String accessToken,HttpServletRequest request){
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
		String key = "oauth2:user:"+accessToken;
		Object obj = redisCache.get(key);
		if(obj != null && obj instanceof Oauth2UserLog){
			Oauth2UserLog log= (Oauth2UserLog)obj;
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
			String openid = getOpenidByToken(assess_token);
			//logger.debug("openid={},assess_token={}",openid,assess_token);
			return redisCache.exists("oauth2:assess_token:"+openid) && 
					assess_token.equals(redisCache.get("oauth2:assess_token:"+openid));
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
		String assess_token = Oauth2AccessToken.getToken();
		try {
			return (Long)redisCache.get("oauth2:user:"+assess_token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void logout() {
		String assess_token = Oauth2AccessToken.getToken();
		String openid;
		logger.debug("assess_token={}",assess_token);
		try {
			if(StringUtils.isNotBlank(assess_token)){
				openid = getOpenidByToken(assess_token);
				String key = "oauth2:assess_token:"+openid;
				if(redisCache.exists(key)){
					Object obj = redisCache.get("oauth2:user:"+assess_token);
					if(obj != null && obj instanceof Oauth2UserLog){
						Oauth2UserLog log= (Oauth2UserLog)obj;
						Oauth2UserLog loginLog = new Oauth2UserLog();
						loginLog.setUserId(log.getUserId());
						loginLog.setCreateTime(new Date());
						loginLog.setHost(log.getHost());
						loginLog.setLogType("LOGOUT");
						loginLog.setPlatformSource(log.getPlatformSource());
						ouath2UserService.saveLoginLog(loginLog);
					}
					redisCache.del(key);
					redisCache.del("oauth2:user:"+assess_token);
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
	
	public Object getSessionAccount(String account){
		if(StringUtils.isNotBlank(account)){
			String openid;
			try {
				openid = openidDes.encrypt(account);
				String openidKey = "oauth2:assess_token:"+openid;
				if(redisCache.exists(openidKey)){
					String otherToken = (String)redisCache.get(openidKey);
					return redisCache.get("oauth2:user:"+otherToken);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
