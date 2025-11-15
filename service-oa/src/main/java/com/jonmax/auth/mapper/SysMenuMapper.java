package com.jonmax.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jonmax.model.system.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author JonMax
 * @since 2025-11-13
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> findMenuListByUserId(@Param("userId") Long userId);
}
