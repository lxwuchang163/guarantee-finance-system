package com.guarantee.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guarantee.finance.entity.AccCarryForward;

import java.util.List;
import java.util.Map;

public interface CarryForwardService extends IService<AccCarryForward> {
    List<Map<String, Object>> previewProfitLoss(String period);
    void carryForwardProfitLoss(String period);
    void reverseCarryForward(Long id);
    List<AccCarryForward> getCarryForwardList(String period);
}
