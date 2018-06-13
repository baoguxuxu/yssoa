package com.lebaoxun.security.oauth2.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 代理后台访问路径检查
 * @author caiqianyi
CREATE TABLE `agent_visit_path` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `path` varchar(200) DEFAULT NULL COMMENT '目录',
  `orderBy` int(11) DEFAULT '0' COMMENT '排序值',
  `enable` char(10) DEFAULT 'Y' COMMENT '是否启用 Y-是 N-禁用',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `path` (`path`) USING BTREE,
  KEY `enable` (`enable`) USING BTREE,
  KEY `createTime` (`createTime`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 *
 */
public class Oauth2VisitPath  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -337992529978848955L;
	private Integer id;
	private String path;
	private Integer orderBy;
	private String enable;
	private Date createTime;
	
	private List<Long> whites;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getEnable() {
		return enable;
	}
	public void setEnable(String enable) {
		this.enable = enable;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Integer getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(Integer orderBy) {
		this.orderBy = orderBy;
	}
	public List<Long> getWhites() {
		return whites;
	}
	public void setWhites(List<Long> whites) {
		this.whites = whites;
	}
}
