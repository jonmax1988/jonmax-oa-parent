package com.jonmax.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jonmax.auth.mapper.SysRoleMapper;
import com.jonmax.auth.service.SysRoleService;
import com.jonmax.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TestMybatisPlus {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRoleService sysRoleService;
    @Test
    public void getAll(){
        List<SysRole> sysRoles = sysRoleMapper.selectList(null);
        System.out.println(sysRoles);
    }

    @Test
    public void addSysRole(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("角色管理员");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理员");
        sysRoleMapper.insert(sysRole);
    }

    @Test
    public void updateSysRole(){
        SysRole sysRole = sysRoleMapper.selectById(9);
        sysRole.setRoleName("角色管理员修改");
        sysRoleMapper.updateById(sysRole);
        System.out.println(sysRole);
    }

    @Test
    public void deleteSysRoleById(){
        sysRoleMapper.deleteById(9);
    }

    @Test
    public void deleteBatch(){
        sysRoleMapper.deleteBatchIds(Arrays.asList(1,2));
    }

    //条件查询
   @Test
    public void testQueryWrapper(){
       QueryWrapper<SysRole> wrapper=new QueryWrapper<>();
       wrapper.eq("role_name","总经理");
       //wrapper.eq("is_deleted",1); SELECT id,role_name,role_code,description,
            //create_time,update_time,is_deleted FROM sys_role WHERE is_deleted=0
                //AND (role_name = ? AND is_deleted = ?)
       List<SysRole> roleList = sysRoleMapper.selectList(wrapper);
       System.out.println(roleList);
   }
   @Test
    public void testLambdaQueryWrapper(){
       LambdaQueryWrapper<SysRole> wrapper=new LambdaQueryWrapper<>();
       wrapper.eq(SysRole::getRoleName,"副总经理");
       List<SysRole> roleList = sysRoleMapper.selectList(wrapper);
       System.out.println(roleList);

   }

   @Test
    public void testService(){
       List<SysRole> list = sysRoleService.list();
       System.out.println(list);
   }

}
