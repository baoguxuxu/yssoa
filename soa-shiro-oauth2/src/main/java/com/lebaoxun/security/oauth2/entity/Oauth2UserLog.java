package com.lebaoxun.security.oauth2.entity;

import java.io.Serializable;
import java.util.Date;

public class Oauth2UserLog implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8664825176409628473L;
	private Long userId;
	private String logType;
	/**
	 * 操作平台 WECAHT,PC,MOBILE
	 */
	private String platformSource;
	private String host;
	private Date createTime;
	private Long timestamp;
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getLogType() {
		return logType;
	}
	public void setLogType(String logType) {
		this.logType = logType;
	}
	public String getPlatformSource() {
		return platformSource;
	}
	public void setPlatformSource(String platformSource) {
		this.platformSource = platformSource;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
