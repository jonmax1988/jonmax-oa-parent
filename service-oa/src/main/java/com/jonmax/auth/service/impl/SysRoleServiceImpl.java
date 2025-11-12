package com.jonmax.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jonmax.auth.mapper.SysRoleMapper;
import com.jonmax.auth.service.SysRoleService;
import com.jonmax.auth.service.SysUserRoleService;
import com.jonmax.model.system.SysRole;
import com.jonmax.model.system.SysUserRole;
import com.jonmax.vo.system.AssginRoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Override
    public Map<String, Object> findRoleDataByUserId(long userId) {
        //1 查询角色 返回所有的角色List集合
        List<SysRole> allRoleList = baseMapper.selectList(null);
        //2 根据userid 查询用户的所有角色
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> existUserRolelist = sysUserRoleService.list(wrapper);
        List<Long> existRoleIdList = existUserRolelist.stream()
                .map(roleId -> roleId.getRoleId()).collect(Collectors.toList());
//        List<Long> RoleIds=new ArrayList<>();
//        for (SysUserRole sysUserRole : existUserRolelist) {
//            Long roleId = sysUserRole.getRoleId();
//            RoleIds.add(roleId);
//        }
        //3 根据用户的角色id ,查询用户的角色信息
        List<SysRole> assignRoleList = new ArrayList<>();
        for (SysRole sysRole : allRoleList) {
            if (existRoleIdList.contains(sysRole.getId())) {
                assignRoleList.add(sysRole);
            }
        }
        //4 把得到的数据整合成map集合
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assignRoleList);
        roleMap.put("allRolesList", allRoleList);
        return roleMap;
    }

    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        // 根据用户ID去删除,用户角色关系表中的数据
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, assginRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);
        //给用户添加新的角色
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        for (Long rolId : roleIdList) {
            if(StringUtils.isEmpty(rolId)){
                continue;
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(assginRoleVo.getUserId());
            sysUserRole.setRoleId(rolId);
            sysUserRoleService.save(sysUserRole);
        }

    }


}
