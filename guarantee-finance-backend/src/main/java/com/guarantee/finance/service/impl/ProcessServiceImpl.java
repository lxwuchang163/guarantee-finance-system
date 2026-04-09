package com.guarantee.finance.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guarantee.finance.dto.ProcessDTO;
import com.guarantee.finance.entity.ApproveRecord;
import com.guarantee.finance.entity.ProcessDefinition;
import com.guarantee.finance.entity.ProcessInstance;
import com.guarantee.finance.entity.ProcessNode;
import com.guarantee.finance.mapper.ApproveRecordMapper;
import com.guarantee.finance.mapper.ProcessDefinitionMapper;
import com.guarantee.finance.mapper.ProcessInstanceMapper;
import com.guarantee.finance.mapper.ProcessNodeMapper;
import com.guarantee.finance.security.LoginUser;
import com.guarantee.finance.service.ProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessServiceImpl extends ServiceImpl<ProcessDefinitionMapper, ProcessDefinition> implements ProcessService {

    @jakarta.annotation.Resource
    private ProcessNodeMapper processNodeMapper;

    @jakarta.annotation.Resource
    private ProcessInstanceMapper processInstanceMapper;

    @jakarta.annotation.Resource
    private ApproveRecordMapper approveRecordMapper;

    @Override
    public ProcessDefinition getDefinition(Long id) {
        ProcessDefinition definition = getById(id);
        if (definition != null) {
            LambdaQueryWrapper<ProcessNode> nodeWrapper = new LambdaQueryWrapper<>();
            nodeWrapper.eq(ProcessNode::getDefinitionId, id);
            nodeWrapper.orderByAsc(ProcessNode::getSortOrder);
            definition.setNodes(processNodeMapper.selectList(nodeWrapper));
        }
        return definition;
    }

    @Override
    public Object getDefinitionDetail(Long id) {
        return getDefinition(id);
    }

    @Override
    public List<?> getDefinitions() {
        LambdaQueryWrapper<ProcessDefinition> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProcessDefinition::getCreateTime);
        List<ProcessDefinition> definitions = list(wrapper);

        for (ProcessDefinition def : definitions) {
            LambdaQueryWrapper<ProcessNode> nodeWrapper = new LambdaQueryWrapper<>();
            nodeWrapper.eq(ProcessNode::getDefinitionId, def.getId());
            nodeWrapper.orderByAsc(ProcessNode::getSortOrder);
            def.setNodes(processNodeMapper.selectList(nodeWrapper));
        }

        return definitions;
    }

    @Override
    public List<ProcessDefinition> listDefinitions(String name, Integer status) {
        LambdaQueryWrapper<ProcessDefinition> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            wrapper.like(ProcessDefinition::getName, name);
        }
        if (status != null) {
            wrapper.eq(ProcessDefinition::getStatus, status);
        }
        wrapper.orderByDesc(ProcessDefinition::getCreateTime);
        List<ProcessDefinition> definitions = list(wrapper);

        for (ProcessDefinition def : definitions) {
            LambdaQueryWrapper<ProcessNode> nodeWrapper = new LambdaQueryWrapper<>();
            nodeWrapper.eq(ProcessNode::getDefinitionId, def.getId());
            nodeWrapper.orderByAsc(ProcessNode::getSortOrder);
            def.setNodes(processNodeMapper.selectList(nodeWrapper));
        }

        return definitions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDefinition(ProcessDTO dto) {
        ProcessDefinition definition = new ProcessDefinition();
        BeanUtils.copyProperties(dto, definition);
        if (definition.getStatus() == null) {
            definition.setStatus(1);
        }
        save(definition);

        if (CollUtil.isNotEmpty(dto.getNodes())) {
            int sortOrder = 0;
            for (ProcessDTO.NodeDTO nodeDto : dto.getNodes()) {
                ProcessNode node = new ProcessNode();
                node.setDefinitionId(definition.getId());
                node.setNodeName(nodeDto.getNodeName());
                node.setNodeType(nodeDto.getNodeType());
                node.setApproverType(nodeDto.getApproverType());
                node.setApproverValue(nodeDto.getApproverValue());
                node.setSortOrder(sortOrder++);
                processNodeMapper.insert(node);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefinition(ProcessDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("流程定义ID不能为空");
        }

        ProcessDefinition definition = new ProcessDefinition();
        BeanUtils.copyProperties(dto, definition);
        updateById(definition);

        // 删除旧节点，重新创建
        LambdaQueryWrapper<ProcessNode> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProcessNode::getDefinitionId, dto.getId());
        processNodeMapper.delete(deleteWrapper);

        if (CollUtil.isNotEmpty(dto.getNodes())) {
            int sortOrder = 0;
            for (ProcessDTO.NodeDTO nodeDto : dto.getNodes()) {
                ProcessNode node = new ProcessNode();
                node.setDefinitionId(definition.getId());
                node.setNodeName(nodeDto.getNodeName());
                node.setNodeType(nodeDto.getNodeType());
                node.setApproverType(nodeDto.getApproverType());
                node.setApproverValue(nodeDto.getApproverValue());
                node.setSortOrder(sortOrder++);
                processNodeMapper.insert(node);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDefinition(Long id) {
        // 检查是否有进行中的流程实例
        LambdaQueryWrapper<ProcessInstance> instanceWrapper = new LambdaQueryWrapper<>();
        instanceWrapper.eq(ProcessInstance::getDefinitionId, id);
        instanceWrapper.in(ProcessInstance::getStatus, Arrays.asList(0, 1)); // 进行中/待审批
        Long runningCount = processInstanceMapper.selectCount(instanceWrapper);
        if (runningCount > 0) {
            throw new RuntimeException("该流程存在进行中的实例，无法删除");
        }

        // 删除关联的节点
        LambdaQueryWrapper<ProcessNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(ProcessNode::getDefinitionId, id);
        processNodeMapper.delete(nodeWrapper);

        return removeById(id);
    }

    @Override
    public void updateDefinitionStatus(Long id, Integer status) {
        ProcessDefinition definition = getById(id);
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        definition.setStatus(status);
        updateById(definition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startProcess(Map<String, Object> params) {
        LoginUser currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        Long definitionId = Long.parseLong(params.get("definitionId").toString());
        Long businessKey = Long.parseLong(params.get("businessKey").toString());
        String businessType = params.get("businessType").toString();

        ProcessDefinition definition = getById(definitionId);
        if (definition == null || definition.getStatus() != 1) {
            throw new RuntimeException("流程定义不存在或已禁用");
        }

        // 创建流程实例
        ProcessInstance instance = new ProcessInstance();
        instance.setDefinitionId(definitionId);
        instance.setBusinessKey(businessKey);
        instance.setBusinessType(businessType);
        instance.setStatus(1); // 待审批
        instance.setCurrentNodeIndex(0);
        instance.setInitiatorId(currentUser.getUserId());
        instance.setInitiatorName(currentUser.getUsername());
        instance.setInitiatorTime(LocalDateTime.now());
        processInstanceMapper.insert(instance);

        // 查询第一个审批节点
        LambdaQueryWrapper<ProcessNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(ProcessNode::getDefinitionId, definitionId);
        nodeWrapper.orderByAsc(ProcessNode::getSortOrder);
        nodeWrapper.last("LIMIT 1");
        ProcessNode firstNode = processNodeMapper.selectOne(nodeWrapper);

        if (firstNode != null) {
            String approver = resolveApprover(firstNode, params);
            instance.setCurrentNodeId(firstNode.getId());
            instance.setCurrentNodeName(firstNode.getNodeName());
            instance.setCurrentApprover(approver);
            processInstanceMapper.updateById(instance);
        } else {
            // 无需审批，直接通过
            instance.setStatus(2); // 已通过
            instance.setCurrentNodeIndex(-1);
            processInstanceMapper.updateById(instance);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("instanceId", instance.getId());
        result.put("status", instance.getStatus());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long instanceId, String opinion, String type) {
        if ("approve".equals(type)) {
            approve(instanceId, opinion);
        } else if ("reject".equals(type)) {
            reject(instanceId, opinion);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long instanceId, String opinion) {
        LoginUser currentUser = getCurrentUser();
        ProcessInstance instance = processInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        if (instance.getStatus() != 1) {
            throw new RuntimeException("当前状态不允许审批操作");
        }

        // 记录审批意见
        ApproveRecord record = new ApproveRecord();
        record.setInstanceId(instanceId);
        record.setNodeId(instance.getCurrentNodeId());
        record.setNodeName(instance.getCurrentNodeName());
        record.setApproverId(currentUser.getUserId());
        record.setApproverName(currentUser.getUsername());
        record.setOpinion(opinion);
        record.setResult("approve");
        record.setApproveTime(LocalDateTime.now());
        approveRecordMapper.insert(record);

        // 查找下一个节点
        int nextIndex = instance.getCurrentNodeIndex() + 1;
        LambdaQueryWrapper<ProcessNode> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(ProcessNode::getDefinitionId, instance.getDefinitionId());
        nodeWrapper.eq(ProcessNode::getSortOrder, nextIndex);
        ProcessNode nextNode = processNodeMapper.selectOne(nodeWrapper);

        if (nextNode != null) {
            instance.setCurrentNodeIndex(nextIndex);
            instance.setCurrentNodeId(nextNode.getId());
            instance.setCurrentNodeName(nextNode.getNodeName());
            Map<String, Object> variables = new HashMap<>();
            String approver = resolveApprover(nextNode, variables);
            instance.setCurrentApprover(approver);
            processInstanceMapper.updateById(instance);
        } else {
            // 所有节点已通过
            instance.setStatus(2); // 已通过
            instance.setCurrentNodeIndex(-1);
            instance.setCurrentNodeId(null);
            instance.setCurrentNodeName(null);
            instance.setCurrentApprover(null);
            processInstanceMapper.updateById(instance);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long instanceId, String opinion) {
        LoginUser currentUser = getCurrentUser();
        ProcessInstance instance = processInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        if (instance.getStatus() != 1) {
            throw new RuntimeException("当前状态不允许驳回操作");
        }

        // 记录审批意见
        ApproveRecord record = new ApproveRecord();
        record.setInstanceId(instanceId);
        record.setNodeId(instance.getCurrentNodeId());
        record.setNodeName(instance.getCurrentNodeName());
        record.setApproverId(currentUser.getUserId());
        record.setApproverName(currentUser.getUsername());
        record.setOpinion(opinion);
        record.setResult("reject");
        record.setApproveTime(LocalDateTime.now());
        approveRecordMapper.insert(record);

        // 更新实例状态为已驳回
        instance.setStatus(3); // 已驳回
        processInstanceMapper.updateById(instance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long instanceId) {
        LoginUser currentUser = getCurrentUser();
        ProcessInstance instance = processInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        if (!instance.getInitiatorId().equals(currentUser.getUserId())) {
            throw new RuntimeException("只有发起人才能撤回流程");
        }
        if (instance.getStatus() != 1) {
            throw new RuntimeException("当前状态不允许撤回操作");
        }

        instance.setStatus(4); // 已撤回
        processInstanceMapper.updateById(instance);
    }

    @Override
    public void cancel(Long instanceId) {
        withdraw(instanceId);
    }

    @Override
    public List<?> getMyPending() {
        LoginUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<ProcessInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessInstance::getCurrentApprover, String.valueOf(currentUser.getUserId()));
        wrapper.eq(ProcessInstance::getStatus, 1); // 待审批
        wrapper.orderByDesc(ProcessInstance::getCreateTime);
        return processInstanceMapper.selectList(wrapper);
    }

    @Override
    public List<?> getMyInitiated() {
        LoginUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<ProcessInstance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessInstance::getInitiatorId, currentUser.getUserId());
        wrapper.orderByDesc(ProcessInstance::getCreateTime);
        return processInstanceMapper.selectList(wrapper);
    }

    @Override
    public List<?> getApproveHistory(Long instanceId) {
        LambdaQueryWrapper<ApproveRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApproveRecord::getInstanceId, instanceId);
        wrapper.orderByAsc(ApproveRecord::getApproveTime);
        return approveRecordMapper.selectList(wrapper);
    }

    @Override
    public ProcessInstance getInstanceDetail(Long instanceId) {
        ProcessInstance instance = processInstanceMapper.selectById(instanceId);
        if (instance != null) {
            // 获取流程定义名称
            ProcessDefinition definition = getById(instance.getDefinitionId());
            if (definition != null) {
                instance.setDefinitionName(definition.getName());
            }
        }
        return instance;
    }

    private LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return (LoginUser) authentication.getPrincipal();
        }
        return null;
    }

    private String resolveApprover(ProcessNode node, Map<String, Object> variables) {
        switch (node.getApproverType()) {
            case "role":
                return "ROLE:" + node.getApproverValue();
            case "user":
                return node.getApproverValue();
            case "specific":
                return node.getApproverValue();
            default:
                return "";
        }
    }
}
