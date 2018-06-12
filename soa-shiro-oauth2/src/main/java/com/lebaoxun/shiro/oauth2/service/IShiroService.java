package com.lebaoxun.shiro.oauth2.service;

import java.util.Set;

import com.lebaoxun.shiro.oauth2.entity.SysUserBase;
import com.lebaoxun.shiro.oauth2.entity.SysUserToken;

/**
 * shiro相关接口
 * @author 蔡骞毅
 * @email 270852221@126.com
 * @date 2018年6月12日 20:32:06
 */
public interface IShiroService {
    /**
     * 获取用户权限列表
     */
    Set<String> getUserPermissions(long userId);

    SysUserToken queryByToken(String token);

    /**
     * 根据用户ID，查询用户
     * @param userId
     */
    SysUserBase queryUser(Long userId);
}
