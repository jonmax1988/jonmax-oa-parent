package com.jonmax.auth.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.jonmax.model.system.SysMenu;
import com.jonmax.vo.system.AssginMenuVo;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-13
 */
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> findNodes();

    void removeMenuById(Long id);

    List<SysMenu> findMenuByRoleId(Long roleId);

    void doAssign(AssginMenuVo assginMenuVo);
}
