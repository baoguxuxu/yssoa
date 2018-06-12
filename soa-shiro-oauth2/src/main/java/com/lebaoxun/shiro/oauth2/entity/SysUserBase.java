package com.lebaoxun.shiro.oauth2.entity;

import java.io.Serializable;

/**
 * 系统用户
 * 
 * @author caiqianyi
 * @email 
 * @date 208年6月13日 上午9:28:55
 */
public class SysUserBase implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 状态  0：禁用   1：正常
	 */
	private Integer status;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
