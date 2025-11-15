package com.jonmax.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jonmax.auth.service.SysRoleService;
import com.jonmax.common.result.Result;
import com.jonmax.model.system.SysRole;
import com.jonmax.vo.system.AssginRoleVo;
import com.jonmax.vo.system.SysRoleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {
    //http://localhost:8800/system/admin/sysRole/findAll
    @Autowired
    private SysRoleService sysRoleService;

    //1 查询所有的角色 和 当前用户的所属的角色
    @ApiOperation("获取用户角色")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable long userId){
        Map<String,Object> map=sysRoleService.findRoleDataByUserId(userId);
        return Result.ok(map);
    }
    //2 为用户分配角色
    @ApiOperation("给用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo){
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }

    //    @GetMapping("/findAll")
//    public List<SysRole> findAll(){
//        List<SysRole> list = sysRoleService.list();
//        return list;
//    }
    @ApiOperation("查询所有的角色")
    @GetMapping("/findAll")
    public Result findAll() {
        List<SysRole> list = sysRoleService.list();
        return Result.ok(list);
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("分页条件查询")
    @GetMapping("{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page,
                                @PathVariable Long limit, SysRoleQueryVo sysRoleQueryVo) {
        IPage<SysRole> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if (!StringUtils.isEmpty(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        IPage<SysRole> pageModel = sysRoleService.page(pageParam, wrapper);
        return Result.ok(pageModel);
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色接口")
    @PostMapping("/save")
    public Result save(@RequestBody SysRole sysRole) {
        boolean save = sysRoleService.save(sysRole);
        if (save) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("根据角色ID查询")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        return Result.ok(role);
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色接口")
    @PutMapping("/update")
    public Result update(@RequestBody SysRole sysRole) {
//        LambdaQueryWrapper<SysRole> wrapper=new LambdaQueryWrapper<>();
//        wrapper.eq(SysRole::getId,sysRole.getId());
//        boolean update = sysRoleService.update(wrapper);
//        if(update){
//            return Result.ok();
//        }else {
//            return Result.fail();
//        }
        boolean updateById = sysRoleService.updateById(sysRole);
        if (updateById) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //根据ID删除
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据ID删除")
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Long id) {
        boolean removedById = sysRoleService.removeById(id);
        if (removedById) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //批量删除
    @ApiOperation("批量删除")
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @DeleteMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        boolean removeByIds = sysRoleService.removeByIds(ids);
        if (removeByIds) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }
}
