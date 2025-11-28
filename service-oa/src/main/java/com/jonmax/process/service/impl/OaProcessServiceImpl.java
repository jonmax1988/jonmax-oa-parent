package com.jonmax.process.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jonmax.auth.service.SysUserService;
import com.jonmax.model.process.OaProcess;
import com.jonmax.model.process.ProcessRecord;
import com.jonmax.model.process.ProcessTemplate;
import com.jonmax.model.system.SysUser;
import com.jonmax.process.mapper.OaProcessMapper;
import com.jonmax.process.service.OaProcessRecordService;
import com.jonmax.process.service.OaProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jonmax.process.service.OaProcessTemplateService;
import com.jonmax.security.custom.LoginUserInfoHelper;
import com.jonmax.vo.process.ApprovalVo;
import com.jonmax.vo.process.ProcessFormVo;
import com.jonmax.vo.process.ProcessQueryVo;
import com.jonmax.vo.process.ProcessVo;
import com.jonmax.wechat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author JonMax
 * @since 2025-11-17
 */
@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, OaProcess> implements OaProcessService {

    @Autowired
    private OaProcessMapper oaProcessMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private OaProcessRecordService processRecordService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private MessageService messageService;

    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> page = oaProcessMapper.selectPage(pageParam, processQueryVo);
        return page;
    }

    @Override
    public void deployByZip(String deployPath) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        Deployment deploy = repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
        System.out.println("==================================" + deploy.getId());
        System.out.println("==================================" + deploy.getName());
    }

    //启动流程
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //1 根据当前用户的id获取用户信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        //2 根据审批模板id把模板信息查询
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());
        //3 保存提交审批信息到业务表 oa_process
        OaProcess process = new OaProcess();
        BeanUtils.copyProperties(processFormVo, process);
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        baseMapper.insert(process);

        //4启动流程实例 RuntimeService
        //4.1 流程定义的key
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        //4.2 业务key processId
        String businessId = String.valueOf(process.getId());
        //4.3 流程参数form表单json数据，转换map集合
        String formValues = processFormVo.getFormValues();
        //formData
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");
        //遍历formData 得到内容，封装到map集合中
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        //流程参数
        Map<String, Object> variables = new HashMap<>();
        variables.put("data", map);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey,
                businessId, variables);
        //5 查询下一个审批人
        //审批人可能是多个
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        List<String> nameList = new ArrayList<>();
        for (Task task : taskList) {
            String assigneeName = task.getAssignee();
            SysUser user = sysUserService.getUserByUserName(assigneeName);
            String name = user.getName();
            nameList.add(name);
            //TODO 6 推送消息
            messageService.pushPendingMessage(process.getId(), sysUser.getId(), task.getId());
        }
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待" + StringUtils.join(nameList.toArray(), ",") + "审批");
        //7 业务和流程进行最终的关联 更新oa_process
        baseMapper.updateById(process);

        //记录操作审批的记录信息
        processRecordService.record(process.getId(), process.getStatus(), process.getDescription());
    }

    //查询待处理的任务列表，分页查询
    @Override
    public IPage<ProcessVo> findPending(Page<ProcessVo> pageParam) {
        //1 封装查询条件，根据当前登录的用户名称
        TaskQuery taskQuery = taskService.createTaskQuery().taskAssignee(LoginUserInfoHelper.getUsername())
                .orderByTaskCreateTime()
                .desc();
        //2 调用方法分页条件查询，返回list集合，待办任务集合
        //listPage中由两个参数，开始位置，每页记录数
        int begin = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) pageParam.getSize();
        List<Task> taskList = taskQuery.listPage(begin, size);
        long totalCount = taskQuery.count();
        //3 封装返回List数据 到List<ProcessVo>里面
        List<ProcessVo> processVoList = new ArrayList<>();
        for (Task task : taskList) {
            //从task任务中获取流程实例id
            String processInstanceId = task.getProcessInstanceId();
            //根据流程实例id获取实例对象
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            //从流程实例对象中获取业务key 就是 processId
            String businessKey = processInstance.getBusinessKey();
            //根据业务key获取Process对象
            if (StringUtils.isEmpty(businessKey)) {
                continue;
            }
            long processId = Long.parseLong(businessKey);
            OaProcess process = baseMapper.selectById(processId);
            //process对象转ProcessVo对象
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId(task.getId());
            processVoList.add(processVo);
        }
        //4 封装返回的IPage对象
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(),
                pageParam.getSize(), totalCount);
        page.setRecords(processVoList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        // 1 根据流程id 获取流程信息Process  oa_process
        OaProcess process = baseMapper.selectById(id);
        //2 根据流程id获取，获取流程记录信息  oa_process_record
        LambdaQueryWrapper<ProcessRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId, id);
        List<ProcessRecord> processRecordList = processRecordService.list(wrapper);
        //3 根据模板id查询模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        Map<String, Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        //4 判断当前用户是否可以进行审批  可以看到信息的不一定能审批，不能重复审批
        boolean isApprove = false;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)) {
            for (Task task : taskList) {
                if (task.getAssignee().equals(LoginUserInfoHelper.getUsername())) {
                    isApprove = true;
                }
            }
        }
        map.put("isApprove", isApprove);
        //5 查询数据封装到map中
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        //1 从approveVo 获取任务id 根据任务id获取流程遍历
        String taskId = approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);
        //2 判断审批状态值 状态值1，审批通过 ； -1 驳回，流程之间结束
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        if (approvalVo.getStatus() == 1) {
            //已通过
            Map<String, Object> variable = new HashMap<String, Object>();
            taskService.complete(taskId, variable);
        } else {
            //驳回结束流程
            this.endTask(taskId);
        }
        //3 记录审批相关过程信息 oa_process_record
        String description = approvalVo.getStatus().intValue() == 1 ? "审批通过" : "驳回";
        processRecordService.record(approvalVo.getProcessId(), approvalVo.getStatus(), description);
        //4 查询下一个审批人 流程id 更新流程表中的记录 process
        OaProcess process = baseMapper.selectById(approvalVo.getProcessId());
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)) {
            List<String> assigneeList = new ArrayList<>();
            for (Task task : taskList) {
                String assignee = task.getAssignee();
                SysUser sysUser = sysUserService.getUserByUserName(assignee);
                assigneeList.add(sysUser.getName());
                //TODO 消息推送
            }
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
            process.setStatus(1);
        } else {
            if (approvalVo.getStatus().intValue() == 1) {
                process.setDescription("审批通过");
                process.setStatus(2);
            } else {
                process.setDescription("审批驳回");
                process.setStatus(-1);
            }
        }
        baseMapper.updateById(process);
    }

    @Override
    public IPage<ProcessVo> findProcessed(Page<OaProcess> pageParam) {
        // 封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished()
                .orderByTaskCreateTime()
                .desc();
        //调用方法 条件分页查询  返回list集合
        //开始位置 和 每页显示记录数
        int begin = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) pageParam.getSize();
        List<HistoricTaskInstance> list = query.listPage(begin, size);
        long totalCount = query.count();
        //遍历list集合 封装List<ProcessVo>
        List<ProcessVo> processVoList = new ArrayList<>();
        for(HistoricTaskInstance item:list){
            //流程实例ID
            String processInstanceId = item.getProcessInstanceId();
            LambdaQueryWrapper<OaProcess> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(OaProcess::getProcessInstanceId,processInstanceId);
            OaProcess process = baseMapper.selectOne(wrapper);
            //process -> processVo
            ProcessVo processVo=new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVoList.add(processVo);
        }
        //Ipage封装分页查询所有数据，返回
        IPage<ProcessVo> pageModel = new Page<>(
                pageParam.getCurrent(),pageParam.getSize(),totalCount);
        pageModel.setRecords(processVoList);
        return pageModel;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo =new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> pageModel = baseMapper.selectPage(pageParam, processQueryVo);
        return pageModel;
    }

    private void endTask(String taskId) {
        //1 根据任务ID  获取task对象
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //2 获取流程定义的模型 BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        //3 获取结束的流向节点
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        //4 当前流向节点
        if (CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = endEventList.get(0);
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        //5 清理当前流动的方向
        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        currentFlowNode.getOutgoingFlows().clear();
        //6 创建新的流向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlow");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        //7 当前节点指向新的方向
        List newSequenceFlowList = new ArrayList();
        newSequenceFlowList.add(newSequenceFlow);
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);
        //8完成当前任务
        taskService.complete(taskId);
    }

    //获取当前的审批信息
    private List<Task> getCurrentTaskList(String id) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(id).list();
        return taskList;
    }
}
