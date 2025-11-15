package com.jonmax.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jonmax.auth.service.SysMenuService;
import com.jonmax.auth.service.SysUserService;
import com.jonmax.common.config.exception.JonMaxException;
import com.jonmax.common.result.Result;
import com.jonmax.common.result.ResultCodeEnum;
import com.jonmax.common.jwt.JwtHelper;
import com.jonmax.common.utils.MD5;
import com.jonmax.model.system.SysUser;
import com.jonmax.vo.system.LoginVo;
import com.jonmax.vo.system.RouterVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 登录
     * @return
     */
    @ApiOperation("用户登录接口")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("token","admin-token");
//        return Result.ok(map);
          //1 获取输入的用户名和密码 查询用户
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,username);
        SysUser sysUser = sysUserService.getOne(wrapper);
        //用户信息判断
        if(sysUser == null){
            throw  new JonMaxException(ResultCodeEnum.FAIL_USER_PASSWORD);
        }
        //4 判断密码 数据库存储的密码，MD5加过密的
        String password = sysUser.getPassword();
        //5 获取用的的密码
        String loginVoPassword = loginVo.getPassword();
        String encrypt_input = MD5.encrypt(loginVoPassword);
        if(!password.equals(encrypt_input)){
            throw  new JonMaxException(ResultCodeEnum.FAIL_USER_PASSWORD);
        }
        // 判断用户是否被禁用 1可用 0禁用
        if(sysUser.getStatus().intValue() == 0){
            throw new JonMaxException(ResultCodeEnum.USER_STATUS_WRONG);
        }
        // 使用jwt根据用户id和用户名称生成token字符串
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        //返回
        Map<String,Object> map =new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }
    /**
     * 获取用户信息
     * @return
     */
    @ApiOperation("获取用户信息接口")
    @GetMapping("info")
    public Result info(HttpServletRequest request) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("roles","[admin]");
//        map.put("name","admin");
//        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
//        return Result.ok(map);
          // 1 从请求头中获取用户信息 获取请求头中的token 字符串
        String token = request.getHeader("token");
        // 2 从token中获取用户的id
        Long userId =JwtHelper.getUserId(token);
        // 3 根据用户的id,查询数据库，把用户信息获取
//        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(SysUser::getId,userId);
        SysUser sysUser = sysUserService.getById(userId);
        // 4 根据用户id获取用户可以操作的菜单列表
            //查询数据库，动态构建的路由结构
       List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);
        // 5 根据用户的id,获取用户可以操作按钮列表perms
       List<String> permsList = sysMenuService.findUserPermsByUserId(userId);
        //6 返回相应的数据

        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        // 返回用户可以操作的菜单
        map.put("routers",routerList);
        //  返回用户可以操作的按钮
        map.put("buttons",permsList);
        return Result.ok(map);
    }
    /**
     * 退出
     * @return
     */
    @ApiOperation("退出登录接口")
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }

}
