package com.jonmax.auth.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jonmax.auth.service.SysUserService;
import com.jonmax.common.result.Result;
import com.jonmax.common.utils.MD5;
import com.jonmax.model.system.SysUser;
import com.jonmax.vo.system.SysUserQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author JonMax
 * @since 2025-11-11
 */
@Api(tags = "用户管理接口")
@RestController
@RequestMapping("/admin/system/sysUser")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;

    @ApiOperation("用户条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page, @PathVariable Long limit, SysUserQueryVo sysUserVo) {
        //获取模糊搜索的关键字 及相关信息
        String userName = sysUserVo.getKeyword();
        String timeBegin = sysUserVo.getCreateTimeBegin();
        String timeEnd = sysUserVo.getCreateTimeEnd();
        //创建page对象
        IPage<SysUser> pageParam = new Page<>(page, limit);
        //封装条件，判断条件的值不为空
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(userName)) {
            wrapper.like(SysUser::getName, userName);
        }
        if (!StringUtils.isEmpty(timeBegin)) {
            wrapper.ge(SysUser::getCreateTime, timeBegin);
        }
        if (!StringUtils.isEmpty(timeEnd)) {
            wrapper.le(SysUser::getCreateTime, timeEnd);
        }
        IPage<SysUser> pageModel = sysUserService.page(pageParam, wrapper);

        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取用户")
    @GetMapping("get/{id}")
    public Result get(@PathVariable long id){
        SysUser user = sysUserService.getById(id);
        return Result.ok(user);
    }
    @ApiOperation(value = "新增用户")
    @PostMapping("save")
    public Result save(@RequestBody SysUser sysUser){
        //对密码进行加密，使用MD5方式加密
        String password = sysUser.getPassword();
        String passwordMD5 = MD5.encrypt(password);
        sysUser.setPassword(passwordMD5);
        sysUserService.save(sysUser);
        return Result.ok();

    }

    @ApiOperation(value = "更新用户")
    @PutMapping("update/{id}")
    public Result update(@RequestBody SysUser sysUser){
        sysUserService.updateById(sysUser);
        return Result.ok();
    }
    @ApiOperation(value = "删除用户")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        sysUserService.removeById(id);
        return Result.ok();
    }
    @ApiOperation(value = "更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable Long id,@PathVariable Integer status){
        sysUserService.updateStatus(id,status);
        return Result.ok();
    }
    @ApiOperation(value = "获取当前用户基本信息")
    @GetMapping("getCurrentUser")
    public Result getCurrentUser() {
        return Result.ok(sysUserService.getCurrentUser());
    }

}

