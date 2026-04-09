package com.guarantee.finance.service;

import com.guarantee.finance.dto.ProcessDTO;
import com.guarantee.finance.entity.ProcessDefinition;
import com.guarantee.finance.entity.ProcessInstance;

import java.util.List;
import java.util.Map;

public interface ProcessService {

    ProcessDefinition getDefinition(Long id);

    Object getDefinitionDetail(Long id);

    List<?> getDefinitions();

    List<ProcessDefinition> listDefinitions(String name, Integer status);

    void createDefinition(ProcessDTO dto);

    void updateDefinition(ProcessDTO dto);

    boolean deleteDefinition(Long id);

    void updateDefinitionStatus(Long id, Integer status);

    Map<String, Object> startProcess(Map<String, Object> params);

    void approve(Long instanceId, String opinion, String type);

    void reject(Long instanceId, String opinion);

    void withdraw(Long instanceId);

    void cancel(Long instanceId);

    List<?> getMyPending();

    List<?> getMyInitiated();

    List<?> getApproveHistory(Long instanceId);

    ProcessInstance getInstanceDetail(Long instanceId);
}
