package com.jonmax.auth.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jonmax.auth.mapper.SysMenuMapper;
import com.jonmax.auth.service.SysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jonmax.auth.service.SysRoleMenuService;
import com.jonmax.auth.utils.MenuHelper;
import com.jonmax.common.config.exception.JonMaxException;
import com.jonmax.common.result.ResultCodeEnum;
import com.jonmax.model.system.SysMenu;
import com.jonmax.model.system.SysRoleMenu;
import com.jonmax.vo.system.AssginMenuVo;
import com.jonmax.vo.system.MetaVo;
import com.jonmax.vo.system.RouterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-13
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> findNodes() {
        //分析：菜单有层级结构，第一层 第二层 第三层 层数不确定 how,递归--》入口~
        //所有菜单数据
        List<SysMenu> sysMenuList = baseMapper.selectList(null);
        //构建
        List<SysMenu> resultList = MenuHelper.buildTree(sysMenuList);
        return resultList;
    }

    @Override
    public void removeMenuById(Long id) {
        //判断当前菜单是否有子菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        Integer count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new JonMaxException(ResultCodeEnum.FAIL);
        }
        baseMapper.deleteById(id);
    }

    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        //全部权限列表
        List<SysMenu> allSysMenuList = this.list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1));

        //根据角色id获取角色权限
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        //转换给角色id与角色权限对应Map对象
        List<Long> menuIdList = sysRoleMenuList.stream().map(e -> e.getMenuId()).collect(Collectors.toList());

        allSysMenuList.forEach(permission -> {
            if (menuIdList.contains(permission.getId())) {
                permission.setSelect(true);
            } else {
                permission.setSelect(false);
            }
        });

        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;
    }

    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        //根据角色id 去删除角色表中的id进行删除
        LambdaQueryWrapper<SysRoleMenu> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,assginMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);
        //遍历assginMenuVo中的list菜单id
        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        menuIdList.stream().filter(item -> !StringUtils.isEmpty(item)).forEach(item -> {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(item);
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
            sysRoleMenuService.save(sysRoleMenu);
        });
    }
    //根据用户id获取用户可以操作菜单列表
    @Override
    public List<RouterVo> findUserMenuListByUserId(Long userId) {
        List<SysMenu> sysMenuList = null;
        // 获取用户的角色信息，判断挡当前是否管理员userId = 1 是管理员
        if(userId.longValue() == 1){
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            //1.1 如果是管理员，查询所有的菜单列表
            //1.2 如果不是管理源，根据userId查询可以操作的菜单列表
            //多表的关联查询 用户id 用户角色关系表  角色菜单关系表 菜单表
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }

        //2 把查询出来的数据列表，构建成框架-要求的路由结构
        //使用菜单的工具类构建成树形结构
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        List<RouterVo> routerList = this.buildRouter(sysMenuTreeList);
        return routerList;
    }

    //构建用户的的路由结构
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        //创建list集合 存储最终数据
        List<RouterVo> routers=new ArrayList<>();
        //menus结合进行封装
        for(SysMenu menu:menus){
            RouterVo router =new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(),menu.getIcon()));
            //下一层数据数据部分
            List<SysMenu> children = menu.getChildren();
            if(menu.getType().intValue() == 1){
                //加载隐藏路由
                List<SysMenu> hiddenMenuList = children.stream()
                        .filter(item -> !StringUtils.isEmpty(item.getComponent()))
                        .collect(Collectors.toList());
                for(SysMenu hiddenMenu:hiddenMenuList){
                    RouterVo hiddenRouter = new RouterVo();
                    //true 表示隐藏路由
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }else{
                if(!CollectionUtils.isEmpty(children)){
                    if(children.size() > 0){
                        router.setAlwaysShow(true);
                    }
                    //递归
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return  routers;
    }

    //5 根据用户的id,获取用户可以操作按钮列表perms
    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        // 判断是否是管理员 所有按钮
        List<SysMenu> sysMenuList = null;
        if(userId.longValue() == 1){
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus,1);
            sysMenuList = baseMapper.selectList(wrapper);
        }else {
            //不是管理员 userId查询可以操作的按钮列表
            sysMenuList= baseMapper.findMenuListByUserId(userId);
        }
            //多表的关联查询 用户id 用户角色关系表  角色菜单关系表 菜单表
        //从查询的数据中 得到可以操作的按钮值，List集合返回
        List<String> permsList = sysMenuList.stream()
                .filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());
        return permsList;
    }
    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }
}
