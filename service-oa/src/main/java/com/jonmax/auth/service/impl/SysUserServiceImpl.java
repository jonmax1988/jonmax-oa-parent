package com.jonmax.auth.service.impl;


import com.jonmax.auth.mapper.SysUserMapper;
import com.jonmax.auth.service.SysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jonmax.model.system.SysUser;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-11
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public void updateStatus(Long id, Integer status) {
        //先根据用户userid查询用户对象
        SysUser sysUser = baseMapper.selectById(id);
        //设置修改的状态值
        sysUser.setStatus(status);
        //更新数据库
        baseMapper.updateById(sysUser);
    }
}
