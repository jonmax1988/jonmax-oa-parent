package com.jonmax.auth.controller;

import com.jonmax.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    /**
     * 登录
     * @return
     */
    @ApiOperation("用户登录接口")
    @PostMapping("login")
    public Result login() {
        Map<String, Object> map = new HashMap<>();
        map.put("token","admin-token");
        return Result.ok(map);
    }
    /**
     * 获取用户信息
     * @return
     */
    @ApiOperation("获取用户信息接口")
    @GetMapping("info")
    public Result info() {
        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name","admin");
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
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
