package com.lebaoxun.security.oauth2.interceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lebaoxun.commons.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.lebaoxun.commons.beans.PropertyConfigurer;
import com.lebaoxun.commons.exception.ResponseMessage;
import com.lebaoxun.security.oauth2.Oauth2AccessToken;
import com.lebaoxun.security.oauth2.Oauth2SecuritySubject;
import com.lebaoxun.soa.core.redis.IRedisCache;

public class AuthorityInterceptor implements HandlerInterceptor {
	
	private static Logger logger = LoggerFactory.getLogger(AuthorityInterceptor.class);
	
	private Properties props = null;
	
	private PropertyConfigurer propertyConfigurer;
	
	@Resource
	private Oauth2SecuritySubject oauth2SecuritySubject;
	
	@Resource
	private IRedisCache redisCache;
	
    /**
     * 拦截指定接口(AuthorityPath内配置)判断所传参数区服和游戏是否属于登录用户本身权限范围内 指定接口
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
    		Object handler) throws Exception {
    	if(HttpServletResponse.SC_NOT_FOUND == response.getStatus()){
    		response.sendRedirect("/404.html");
    		return true;
    	}
    	String token = request.getHeader("Authorization");
    	if(StringUtils.isBlank(token)){
    		token = request.getHeader("token");
    	}
    	if(StringUtils.isBlank(token)){
    		token = request.getParameter("token");
    	}
    	logger.debug("token={}",token);
    	Oauth2AccessToken.setToken(token);
    	Long userId = null;
    	if(StringUtils.isBlank(token) || "null".equals(token)
    			||  !oauth2SecuritySubject.checkToken(token) || 
    			(userId = oauth2SecuritySubject.getCurrentUser()) == null){
    		writeError(response, "10003");
    		return false;
    	}
    	
    	String path = request.getRequestURI().replace(request.getContextPath(), "");
    	if(!oauth2SecuritySubject.isWhiteAccess(userId, path)){
    		writeError(response, "10008");
			return false;
    	}
    	
    	oauth2SecuritySubject.updateLog(token, request);
        return true;
    }
    
    private void writeError(HttpServletResponse response,String code) throws IOException{
    	Gson gson = new Gson();
		ResponseMessage _i18n = new ResponseMessage();
		String msg = "未知错误！";
		if(code != null && props != null && props.containsKey(code)){
			msg = props.getProperty(code);
		}else{
			logger.error("errcode no found ‘{}’",code);
		}
		_i18n.setErrcode(code);
		_i18n.setErrmsg(msg);
		String jsonString = gson.toJson(_i18n);
		response.setContentType("application/json;charset=utf-8");
		PrintWriter writer = response.getWriter();
		writer.write(jsonString);
		writer.flush();
    }

    @Resource
	public void setPropertyConfigurer(PropertyConfigurer propertyConfigurer) {
		this.propertyConfigurer = propertyConfigurer;
		this.props = this.propertyConfigurer.lazyLoadUniqueProperties("classpath*:i18n_messages.properties");
	}

	@Override
	public void afterCompletion(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub
		logger.debug("oauth2 token remove");
		Oauth2AccessToken.remove();
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1,
			Object arg2, ModelAndView arg3) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
