package com.jonmax.activiti;

import com.jonmax.ServiceAuthApplication;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

@SpringBootTest(classes = ServiceAuthApplication.class)
public class ProcessTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    //单个流程实例挂起
    @Test
    public void SingleSuspendProcessInstance() {
        String processInstanceId = "8bdff984-ab53-11ed-9b17-f8e43b734677";
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        //获取到当前流程定义是否为暂停状态   suspended方法为true代表为暂停   false就是运行的
        boolean suspended = processInstance.isSuspended();
        if (suspended) {
            runtimeService.activateProcessInstanceById(processInstanceId);
            System.out.println("流程实例:" + processInstanceId + "激活");
        } else {
            runtimeService.suspendProcessInstanceById(processInstanceId);
            System.out.println("流程实例:" + processInstanceId + "挂起");
        }
    }

    //全部流程实例挂起
    @Test
    public void suspendProcessInstance() {
        //获取流程定义的对象
        ProcessDefinition qingjia = repositoryService.createProcessDefinitionQuery().processDefinitionKey("qingjia").singleResult();
        boolean suspended = qingjia.isSuspended();
        //调用流程定义中的方法，获取流程状态判断  激活/挂起  true=挂起
        if(suspended){
            //流程定义的ID   boolean 是否激活 true激活     第三个参数 时间点
            repositoryService.activateProcessDefinitionById(qingjia.getId(),true,null);
            System.out.println("qingjia.getId()激活成功：activateProcessDefinitionById"+qingjia.getId());
        }else {
            repositoryService.suspendProcessDefinitionById(qingjia.getId(),false,null);
            System.out.println("qingjia.getId()挂起成功：suspendProcessDefinitionById"+qingjia.getId());
        }
    }

    //创建流程实例，指定businessKey
    @Test
    public void startUpWithBusinessKey(){
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("qingjia", "1");
        System.out.println(instance.getBusinessKey());
    }
    //查询已处理的任务
    @Test
    public void findHisTaskList(){
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssignee("zhangsan").finished().list();
        list.forEach(item -> {
            System.out.println("实例ID："+item.getProcessInstanceId());
            System.out.println("任务id：" + item.getId());
            System.out.println("任务负责人：" + item.getAssignee());
            System.out.println("任务名称：" + item.getName());
        });
    }

    //处理当前任务
    @Test
    public void doTask(){
        Task task = taskService.createTaskQuery().taskAssignee("hsl").singleResult();
        taskService.complete("===================="+task.getId());
    }
    //查询当前任务
    @Test
    public void findTaskList(){
        String assign = "lxx";
        List<Task> list = taskService.createTaskQuery().taskAssignee(assign).list();
        list.forEach(item -> {
            System.out.println("流程实例ID"+item.getProcessInstanceId());
            System.out.println("任务id：" + item.getId());
            System.out.println("任务负责人：" + item.getAssignee());
            System.out.println("任务名称：" + item.getName());
        });


    }

    //启动流程实力
    @Test
    public void startProcess(){
        //流程图中，Process identifier
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("qingjia");
        System.out.println("流程定义的ID:"+processInstance.getProcessDefinitionId());
        System.out.println("流程实例的ID:"+processInstance.getId());
        System.out.println("流程活动的ID:"+processInstance.getActivityId());
    }
    //单个文件部署方式
    @Test
    public void deployProcess(){
        //System.out.println("RepositoryService:============= " + repositoryService);
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment.addClasspathResource("process/qingjia.bpmn20.xml")
                .addClasspathResource("process/qingjia.png")
                .name("请假流程的部署")
                .deploy();
        System.out.println(deploy.getId());
        System.out.println(deploy.getName());
    }

    //压缩包部署
    @Test
    public void deployProcessByZip() {
        // 定义zip输入流
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream(
                        "process/qingjia.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        // 流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .name("请假申请流程")
                .deploy();
        System.out.println("流程部署id：" + deployment.getId());
        System.out.println("流程部署名称：" + deployment.getName());
    }

    /**
     * 查询流程定义
     */
    @Test
    public void findProcessDefinitionList(){
        List<ProcessDefinition> definitionList = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        //输出流程定义信息
        for (ProcessDefinition processDefinition : definitionList) {
            System.out.println("流程定义 id="+processDefinition.getId());
            System.out.println("流程定义 name="+processDefinition.getName());
            System.out.println("流程定义 key="+processDefinition.getKey());
            System.out.println("流程定义 Version="+processDefinition.getVersion());
            System.out.println("流程部署ID ="+processDefinition.getDeploymentId());
        }
    }

    /**
     * 删除流程定义
     */
    public void deleteDeployment() {
        //部署id
        String deploymentId = "82e3bc6b-81da-11ed-8e03-7c57581a7819";
        //删除流程定义，如果该流程定义已有流程实例启动则删除时出错
        repositoryService.deleteDeployment(deploymentId);
        //设置true 级联删除流程定义，即使该流程有流程实例启动也可以删除，设置为false非级别删除方式
        //repositoryService.deleteDeployment(deploymentId, true);
    }

}
