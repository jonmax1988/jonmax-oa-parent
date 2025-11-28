package com.jonmax.process.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jonmax.model.process.OaProcess;
import com.jonmax.vo.process.ApprovalVo;
import com.jonmax.vo.process.ProcessFormVo;
import com.jonmax.vo.process.ProcessQueryVo;
import com.jonmax.vo.process.ProcessVo;

import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-17
 */
public interface OaProcessService extends IService<OaProcess> {

    //审批列表管理
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo);
    //部署流程定义
    void deployByZip(String deployPath);

    void startUp(ProcessFormVo processFormVo);

    IPage<ProcessVo> findPending(Page<ProcessVo> pageParam);

    Map<String, Object> show(Long id);

    void approve(ApprovalVo approvalVo);

    IPage<ProcessVo> findProcessed(Page<OaProcess> pageParam);

    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);
}
