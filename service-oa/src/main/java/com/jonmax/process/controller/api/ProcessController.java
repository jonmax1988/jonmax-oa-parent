package com.jonmax.process.controller.api;

import com.jonmax.common.result.Result;
import com.jonmax.model.process.ProcessTemplate;
import com.jonmax.model.process.ProcessType;
import com.jonmax.process.service.OaProcessTemplateService;
import com.jonmax.process.service.OaProcessTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "审批流管理")
@RestController
@RequestMapping(value="/admin/process")
@CrossOrigin  //跨域
public class ProcessController {
    @Autowired
    private OaProcessTypeService oaProcessTypeService;

    @Autowired
    private OaProcessTemplateService processTemplateService;
    @ApiOperation(value = "获取全部审批分类及模板")
    @GetMapping("findProcessType")
    public Result findProcessType(){
       List<ProcessType> list= oaProcessTypeService.findProcessType();
       return Result.ok(list);
    }

    //获取审批模板数据
    @GetMapping("getProcessTemplate/{processTemplateId}")
    public Result getProcessTemplate(@PathVariable Long processTemplateId) {
        ProcessTemplate processTemplate = processTemplateService.getById(processTemplateId);
        return Result.ok(processTemplate);
    }

}
