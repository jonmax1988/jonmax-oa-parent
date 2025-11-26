package com.jonmax.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jonmax.model.process.ProcessTemplate;
import com.jonmax.model.process.ProcessType;
import com.jonmax.process.mapper.OaProcessTemplateMapper;
import com.jonmax.process.service.OaProcessService;
import com.jonmax.process.service.OaProcessTemplateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jonmax.process.service.OaProcessTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-16
 */
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate>
        implements OaProcessTemplateService {

    @Resource
    private OaProcessTemplateMapper processTemplateMapper;

    @Resource
    private OaProcessTypeService processTypeService;

    @Autowired
    private OaProcessService oaProcessService;

    @Override
    public IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageParam) {
        LambdaQueryWrapper<ProcessTemplate> queryWrapper = new LambdaQueryWrapper<ProcessTemplate>();
        queryWrapper.orderByDesc(ProcessTemplate::getId);
        IPage<ProcessTemplate> page = processTemplateMapper.selectPage(pageParam, queryWrapper);
        List<ProcessTemplate> processTemplateList = page.getRecords();

        List<Long> processTypeIdList = processTemplateList.stream().map(processTemplate -> processTemplate.getProcessTypeId()).collect(Collectors.toList());

        if(!CollectionUtils.isEmpty(processTypeIdList)) {
            Map<Long, ProcessType> processTypeIdToProcessTypeMap = processTypeService.list(new LambdaQueryWrapper<ProcessType>().in(ProcessType::getId, processTypeIdList)).stream().collect(Collectors.toMap(ProcessType::getId, ProcessType -> ProcessType));
            for(ProcessTemplate processTemplate : processTemplateList) {
                ProcessType processType = processTypeIdToProcessTypeMap.get(processTemplate.getProcessTypeId());
                if(null == processType) continue;
                processTemplate.setProcessTypeName(processType.getName());
            }
        }
        return page;
    }

    @Override
    public void publish(Long id) {
        ProcessTemplate processTemplate = this.getById(id);
        processTemplate.setStatus(1);
        processTemplateMapper.updateById(processTemplate);
        //TODO 部署流程定义，后续完善
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())){
            oaProcessService.deployByZip(processTemplate.getProcessDefinitionPath());
        }
    }
}



