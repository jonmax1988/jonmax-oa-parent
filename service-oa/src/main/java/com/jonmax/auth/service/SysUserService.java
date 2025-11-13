package com.jonmax.auth.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jonmax.model.system.SysUser;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-11
 */
public interface SysUserService extends IService<SysUser> {

    void updateStatus(Long id, Integer status);
}
