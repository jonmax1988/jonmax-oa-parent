package com.jonmax.process.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jonmax.model.process.OaProcess;
import com.jonmax.vo.process.ProcessQueryVo;
import com.jonmax.vo.process.ProcessVo;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-17
 */
public interface OaProcessService extends IService<OaProcess> {

    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);
}
