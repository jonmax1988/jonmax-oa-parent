package com.jonmax.auth.utils;

import com.jonmax.model.system.SysMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuHelper {
    //递归方法建菜单,入口 和 出口条件。
    public static List<SysMenu> buildTree(List<SysMenu> sysMenuList) {
        //创建一个List集合
        List<SysMenu> trees= new ArrayList<>();
        for(SysMenu sysMenu:sysMenuList){
            //递归入口 parentId=0 作为入口
            if(sysMenu.getParentId().longValue() == 0){
                trees.add(getChildren(sysMenu,sysMenuList));
            }

        }
        return trees;
    }
    public static SysMenu getChildren(SysMenu sysMenu,List<SysMenu> sysMenuList){
        sysMenu.setChildren(new ArrayList<SysMenu>());
        //遍历所有的菜单数据，判断id 和 parentId 对应关系
        for(SysMenu it:sysMenuList){
            if(sysMenu.getId().longValue() == it.getParentId().longValue()){
                if(sysMenu.getChildren() == null){
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(getChildren(it,sysMenuList));
            }
        }
        return sysMenu;
    }
}
