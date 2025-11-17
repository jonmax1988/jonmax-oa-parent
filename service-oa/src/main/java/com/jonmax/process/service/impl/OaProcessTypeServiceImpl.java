package com.jonmax.process.service.impl;

import com.jonmax.model.process.ProcessType;
import com.jonmax.process.mapper.OaProcessTypeMapper;
import com.jonmax.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-16
 */
@Service
public class OaProcessTypeServiceImpl extends ServiceImpl<OaProcessTypeMapper, ProcessType> implements OaProcessTypeService {

}
