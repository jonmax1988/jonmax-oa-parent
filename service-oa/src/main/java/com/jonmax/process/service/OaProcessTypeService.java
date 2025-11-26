package com.jonmax.process.service;

import com.jonmax.model.process.ProcessType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-16
 */
public interface OaProcessTypeService extends IService<ProcessType> {

    List<ProcessType> findProcessType();
}
