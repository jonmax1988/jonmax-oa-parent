package com.jonmax.process.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jonmax.model.process.OaProcess;
import com.jonmax.process.mapper.OaProcessMapper;
import com.jonmax.process.service.OaProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jonmax.vo.process.ProcessQueryVo;
import com.jonmax.vo.process.ProcessVo;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
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
    RepositoryService repositoryService;
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> page = oaProcessMapper.selectPage(pageParam, processQueryVo);
        return page;
    }

    @Override
    public void deployByZip(String deployPath) {
        InputStream inputStream=this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        Deployment deploy = repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
        System.out.println("=================================="+deploy.getId());
        System.out.println("=================================="+deploy.getName());
    }
}
