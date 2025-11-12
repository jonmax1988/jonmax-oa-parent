package com.jonmax.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jonmax.model.system.SysRole;
import com.jonmax.vo.system.AssginRoleVo;

import java.util.Map;

public interface SysRoleService extends IService<SysRole> {
    Map<String,Object> findRoleDataByUserId(long userId);

    void doAssign(AssginRoleVo assginRoleVo);
}
