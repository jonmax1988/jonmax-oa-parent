package com.jonmax.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jonmax.model.base.BaseEntity;
import com.jonmax.model.process.ProcessTemplate;
import com.jonmax.model.process.ProcessType;
import com.jonmax.process.mapper.OaProcessTypeMapper;
import com.jonmax.process.service.OaProcessTemplateService;
import com.jonmax.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    OaProcessTemplateService oaProcessTemplateService;
    @Override
    public List<ProcessType> findProcessType() {
        //查询所有的审批分类，返回list集合
        List<ProcessType> processTypeList = baseMapper.selectList(null);
        //遍历所有的审批分类list集合
        //List<Long> idList = processTypeList.stream().map(ProcessType::getId).collect(Collectors.toList());
        //得到每个审批分类，根据审批分类id查询对于的审批模板
        for(ProcessType processType:processTypeList){
            LambdaQueryWrapper<ProcessTemplate> wrapper=new LambdaQueryWrapper<>();
            Long typeId= processType.getId();
            wrapper.eq(ProcessTemplate::getProcessTypeId,typeId);
            List<ProcessTemplate> processTemplate = oaProcessTemplateService.list(wrapper);
            processType.setProcessTemplateList(processTemplate);
        }
        //根据审批分类的ID，查询对应的审批模板的数据封装到每个审批分类对象里面
        return processTypeList;
    }
}
