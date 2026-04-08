package com.guarantee.finance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarantee.finance.config.NcCloudConfig;
import com.guarantee.finance.dto.NcCustomerSyncDTO;
import com.guarantee.finance.dto.NcVoucherSyncDTO;
import com.guarantee.finance.entity.NcSyncError;
import com.guarantee.finance.entity.NcSyncLog;
import com.guarantee.finance.mapper.NcSyncErrorMapper;
import com.guarantee.finance.mapper.NcSyncLogMapper;
import com.guarantee.finance.service.NcCloudService;
import com.guarantee.finance.vo.NcSyncLogVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NcCloudServiceImpl extends ServiceImpl<NcSyncLogMapper, NcSyncLog> implements NcCloudService {

    private static final Logger log = LoggerFactory.getLogger(NcCloudServiceImpl.class);

    @Autowired
    private NcCloudConfig ncConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @jakarta.annotation.Resource
    private NcSyncErrorMapper ncSyncErrorMapper;

    private String cachedToken = null;
    private LocalDateTime tokenExpireTime = null;

    @Override
    public boolean login() {
        try {
            String url = ncConfig.getBaseUrl() + "/nccloud/api/uapbd/arap/accvoucherbill/login";
            Map<String, Object> body = new HashMap<>();
            body.put("username", ncConfig.getUsername());
            body.put("password", ncConfig.getPassword());
            body.put("client_id", ncConfig.getClientId());
            body.put("client_secret", ncConfig.getClientSecret());

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode dataNode = json.get("data");
                if (dataNode != null) {
                    cachedToken = dataNode.asText();
                    tokenExpireTime = LocalDateTime.now().plusHours(2); // Token有效期2小时
                    log.info("NC Cloud登录成功，Token已获取");
                    return true;
                }
            }
            log.error("NC Cloud登录失败: {}", response.getBody());
            return false;
        } catch (Exception e) {
            log.error("NC Cloud登录异常", e);
            return false;
        }
    }

    @Override
    public void logout() {
        cachedToken = null;
        tokenExpireTime = null;
        log.info("NC Cloud已登出");
    }

    @Override
    public String getToken() {
        if (!isTokenValid()) {
            login();
        }
        return cachedToken;
    }

    @Override
    public boolean isTokenValid() {
        return StrUtil.isNotBlank(cachedToken) && tokenExpireTime != null && LocalDateTime.now().isBefore(tokenExpireTime);
    }

    @Override
    public boolean syncCustomer(NcCustomerSyncDTO dto) {
        long startTime = System.currentTimeMillis();
        NcSyncLog syncLog = createSyncLog("CUSTOMER", dto.getCustomerCode(), dto);

        try {
            // 构建NC Cloud客户档案数据结构
            Map<String, Object> ncCustomerData = buildNcCustomerMap(dto);

            // 调用NC Cloud接口
            String url = ncConfig.getBaseUrl() + "/nccloud/api/uapbd/customer/save";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(ncCustomerData, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                boolean success = checkNcResult(result);
                updateSyncResult(syncLog, success ? 2 : 3, success ? null : "NC返回失败",
                        objectMapper.writeValueAsString(result), System.currentTimeMillis() - startTime);
                return success;
            } else {
                updateSyncResult(syncLog, 3, "HTTP错误: " + response.getStatusCode(), response.getBody(),
                        System.currentTimeMillis() - startTime);
                return false;
            }
        } catch (Exception e) {
            log.error("同步客户[{}]到NC Cloud失败: {}", dto.getCustomerCode(), e.getMessage(), e);
            saveSyncError(syncLog.getId(), "SYSTEM", e.getMessage());
            updateSyncResult(syncLog, 3, e.getMessage(), null, System.currentTimeMillis() - startTime);
            return false;
        }
    }

    @Override
    public boolean syncBatchCustomers(List<NcCustomerSyncDTO> dtoList) {
        int successCount = 0;
        int failCount = 0;
        for (NcCustomerSyncDTO dto : dtoList) {
            if (syncCustomer(dto)) {
                successCount++;
            } else {
                failCount++;
            }
        }
        log.info("批量同步客户完成：成功{}，失败{}", successCount, failCount);
        return failCount == 0;
    }

    @Override
    public boolean syncVoucher(NcVoucherSyncDTO dto) {
        long startTime = System.currentTimeMillis();
        NcSyncLog syncLog = createSyncLog("VOUCHER", dto.getVoucherNo(), dto);

        try {
            // 构建NC Cloud凭证数据结构
            Map<String, Object> ncVoucherData = buildNcVoucherMap(dto);

            // 调用NC Cloud凭证接口
            String url = ncConfig.getBaseUrl() + "/nccloud/api/gl/accvoucher/save";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(ncVoucherData, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode result = objectMapper.readTree(response.getBody());
                boolean success = checkNcResult(result);
                updateSyncResult(syncLog, success ? 2 : 3, success ? null : "凭证同步失败",
                        objectMapper.writeValueAsString(result), System.currentTimeMillis() - startTime);
                return success;
            } else {
                updateSyncResult(syncLog, 3, "HTTP错误: " + response.getStatusCode(), response.getBody(),
                        System.currentTimeMillis() - startTime);
                return false;
            }
        } catch (Exception e) {
            log.error("同步凭证[{}]到NC Cloud失败: {}", dto.getVoucherNo(), e.getMessage(), e);
            saveSyncError(syncLog.getId(), "SYSTEM", e.getMessage());
            updateSyncResult(syncLog, 3, e.getMessage(), null, System.currentTimeMillis() - startTime);
            return false;
        }
    }

    @Override
    public boolean syncBatchVouchers(List<NcVoucherSyncDTO> dtoList) {
        int successCount = 0;
        int failCount = 0;
        for (NcVoucherSyncDTO dto : dtoList) {
            if (syncVoucher(dto)) {
                successCount++;
            } else {
                failCount++;
            }
        }
        log.info("批量同步凭证完成：成功{}，失败{}", successCount, failCount);
        return failCount == 0;
    }

    @Override
    public IPage<NcSyncLogVO> querySyncLog(String syncType, Integer status, Page<?> page) {
        LambdaQueryWrapper<NcSyncLog> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(syncType)) {
            wrapper.eq(NcSyncLog::getSyncType, syncType);
        }
        if (status != null) {
            wrapper.eq(NcSyncLog::getStatus, status);
        }
        wrapper.orderByDesc(NcSyncLog::getCreateTime);

        IPage<NcSyncLog> logPage = this.page(page.convert(p -> new NcSyncLog()), wrapper);
        IPage<NcSyncLogVO> voPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        List<NcSyncLogVO> voList = new ArrayList<>();
        for (NcSyncLog logRecord : logPage.getRecords()) {
            NcSyncLogVO vo = new NcSyncLogVO();
            vo.setId(logRecord.getId());
            vo.setSyncType(logRecord.getSyncType());
            vo.setBusinessKey(logRecord.getBusinessKey());
            vo.setStatus(logRecord.getStatus());
            vo.setStatusText(getStatusText(logRecord.getStatus()));
            vo.setErrorMessage(logRecord.getErrorMessage());
            vo.setRetryCount(logRecord.getRetryCount());
            vo.setDurationMs(logRecord.getDurationMs());
            vo.setSyncTime(logRecord.getSyncTime());
            vo.setCreateTime(logRecord.getCreateTime());
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public NcSyncLogVO getSyncDetail(Long id) {
        NcSyncLog logRecord = getById(id);
        if (logRecord == null) return null;
        NcSyncLogVO vo = new NcSyncLogVO();
        vo.setId(logRecord.getId());
        vo.setSyncType(logRecord.getSyncType());
        vo.setBusinessKey(logRecord.getBusinessKey());
        vo.setStatus(logRecord.getStatus());
        vo.setStatusText(getStatusText(logRecord.getStatus()));
        vo.setErrorMessage(logRecord.getErrorMessage());
        vo.setRetryCount(logRecord.getRetryCount());
        vo.setDurationMs(logRecord.getDurationMs());
        vo.setSyncTime(logRecord.getSyncTime());
        vo.setCreateTime(logRecord.getCreateTime());
        return vo;
    }

    @Override
    public boolean retrySync(Long logId) {
        NcSyncLog syncLog = getById(logId);
        if (syncLog == null) throw new RuntimeException("同步日志不存在");
        if (syncLog.getStatus() == 2) throw new RuntimeException("该记录已成功，无需重试");

        syncLog.setRetryCount((syncLog.getRetryCount() == null ? 0 : syncLog.getRetryCount()) + 1);
        updateById(syncLog);

        if ("CUSTOMER".equals(syncLog.getSyncType())) {
            // 根据businessKey重新查询客户并同步
            NcCustomerSyncDTO dto = rebuildCustomerDTO(syncLog);
            if (dto != null) return syncCustomer(dto);
        } else if ("VOUCHER".equals(syncLog.getSyncType())) {
            NcVoucherSyncDTO dto = rebuildVoucherDTO(syncLog);
            if (dto != null) return syncVoucher(dto);
        }
        return false;
    }

    @Override
    public Map<String, Object> checkCustomerDiff(String customerCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("localCustomer", getLocalCustomerInfo(customerCode));
        result.put("ncCustomer", getNcCustomer(customerCode));
        result.put("diffFields", compareCustomerDiff(customerCode));
        return result;
    }

    @Override
    public Map<String, Object> getNcCustomer(String customerCode) {
        try {
            String url = ncConfig.getBaseUrl() + "/nccloud/api/uapbd/customer/getByCode?code=" + customerCode;
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                JsonNode data = json.get("data");
                if (data != null) {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("customerName", pathText(data, "name"));
                    customer.put("shortName", pathText(data, "shortname"));
                    customer.put("custType", pathText(data, "custtype"));
                    customer.put("creditCode", pathText(data, "defitem1"));
                    customer.put("phone", pathText(data, "phone"));
                    customer.put("address", pathText(data, "address"));
                    customer.put("isEnabled", pathText(data, "isenable"));
                    return customer;
                }
            }
        } catch (Exception e) {
            log.error("获取NC客户信息失败: {}", e.getMessage());
        }
        return Collections.emptyMap();
    }

    // ==================== 私有方法 ====================

    private NcSyncLog createSyncLog(String syncType, String businessKey, Object data) {
        NcSyncLog syncLog = new NcSyncLog();
        syncLog.setSyncType(syncType);
        syncLog.setBusinessKey(businessKey);
        try {
            syncLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception ignored) {}
        syncLog.setStatus(1); // 同步中
        syncLog.setRetryCount(0);
        syncLog.setSyncTime(LocalDateTime.now().toString().replace("T", " "));
        save(syncLog);
        return syncLog;
    }

    private void updateSyncResult(NcSyncLog syncLog, int status, String errorMsg, String ncResponse, long durationMs) {
        syncLog.setStatus(status);
        syncLog.setErrorMsg(errorMsg);
        syncLog.setNcResponse(ncResponse);
        syncLog.setDurationMs(durationMs);
        updateById(syncLog);
    }

    private void saveSyncError(Long logId, String errorType, String message) {
        NcSyncError error = new NcSyncError();
        error.setLogId(logId);
        error.setErrorType(errorType);
        error.setErrorMessage(message);
        error.setStatus(0);
        error.setRetryCount(0);
        error.setMaxRetryCount(5);
        ncSyncErrorMapper.insert(error);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = createHeaders();
        headers.set("Authorization", "Bearer " + getToken());
        headers.set("DataSource", ncConfig.getDataSource());
        headers.set("GroupCode", ncConfig.getGroupCode());
        return headers;
    }

    private Map<String, Object> buildNcCustomerMap(NcCustomerSyncDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("pk_customer", dto.getCustomerCode());
        map.put("name", dto.getCustomerName());
        map.put("shortname", dto.getCustomerShortName() != null ? dto.getCustomerShortName() : dto.getCustomerName());
        map.put("custtype", convertCustomerType(dto.getCustomerType()));
        map.put("defitem1", dto.getCreditCode()); // 统一社会信用代码
        map.put("indclass", dto.getIndustry());
        map.put("phone", dto.getContactPhone());
        map.put("address", dto.getAddress());
        map.put("isenable", dto.getStatus() != null && dto.getStatus() == 1 ? "1" : "0");
        return map;
    }

    private Map<String, Object> buildNcVoucherMap(NcVoucherSyncDTO dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("voucher_no", dto.getVoucherNo());
        map.put("voucher_date", dto.getVoucherDate());
        map.put("voucher_type", convertVoucherType(dto.getVoucherType()));
        map.put("accounting_period", dto.getAccountingPeriod());
        map.put("company_code", dto.getCompanyCode());
        map.put("maker", dto.getMaker());
        map.put("remark", dto.getRemark());
        map.put("total_debit", dto.getTotalDebit());
        map.put("total_credit", dto.getTotalCredit());

        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            List<Map<String, Object>> details = new ArrayList<>();
            int index = 1;
            for (NcVoucherSyncDTO.VoucherDetailSyncDTO detail : dto.getDetails()) {
                Map<String, Object> detailMap = new HashMap<>();
                detailMap.put("line_no", index++);
                detailMap.put("account_code", detail.getAccountCode());
                detailMap.put("account_name", detail.getAccountName());
                detailMap.put("direction", detail.getDirection());
                detailMap.put("amount", detail.getAmount());
                detailMap.put("summary", detail.getSummary());
                detailMap.put("customer_code", detail.getCustomerCode());
                detailMap.put("department_code", detail.getDepartmentCode());
                detailMap.put("project_code", detail.getProjectCode());
                details.add(detailMap);
            }
            map.put("details", details);
        }
        return map;
    }

    private String convertCustomerType(Integer type) {
        switch (type) {
            case 1: return "01"; // 个人
            case 2: return "02"; // 企业
            case 3: return "03"; // 机构
            default: return "02";
        }
    }

    private Integer convertVoucherType(Integer type) {
        switch (type) {
            case 1: return 1; // 收款凭证
            case 2: return 2; // 付款凭证
            case 3: return 3; // 转账凭证
            default: return 3;
        }
    }

    private boolean checkNcResult(JsonNode result) {
        if (result == null) return false;
        JsonNode code = result.get("code");
        return code != null && "200".equals(code.asText());
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待同步";
            case 1: return "同步中";
            case 2: return "成功";
            case 3: return "失败";
            case 4: return "部分成功";
            default: return "未知";
        }
    }

    private String pathText(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return child != null ? child.asText("") : "";
    }

    private NcCustomerSyncDTO rebuildCustomerDTO(NcSyncLog syncLog) {
        try {
            if (StrUtil.isBlank(syncLog.getBusinessData())) return null;
            return objectMapper.readValue(syncLog.getBusinessData(), NcCustomerSyncDTO.class);
        } catch (Exception e) {
            log.warn("重建客户DTO失败: {}", e.getMessage());
            return null;
        }
    }

    private NcVoucherSyncDTO rebuildVoucherDTO(NcSyncLog syncLog) {
        try {
            if (StrUtil.isBlank(syncLog.getBusinessData())) return null;
            return objectMapper.readValue(syncLog.getBusinessData(), NcVoucherSyncDTO.class);
        } catch (Exception e) {
            log.warn("重建凭证DTO失败: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> getLocalCustomerInfo(String customerCode) {
        Map<String, Object> info = new HashMap<>();
        info.put("customerCode", customerCode);
        return info;
    }

    private Map<String, Object> compareCustomerDiff(String customerCode) {
        Map<String, Object> local = (Map<String, Object>) getLocalCustomerInfo(customerCode);
        Map<String, Object> nc = getNcCustomer(customerCode);
        Map<String, Object> diff = new HashMap<>();

        for (String key : Arrays.asList("customerName", "shortName", "phone", "address")) {
            String localVal = local.get(key) != null ? local.get(key).toString() : "";
            String ncVal = nc.get(key) != null ? nc.get(key).toString() : "";
            if (!localVal.equals(ncVal)) {
                diff.put(key, Map.of("local", localVal, "nc", ncVal));
            }
        }
        return diff;
    }
}
