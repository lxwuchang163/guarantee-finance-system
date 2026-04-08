package com.guarantee.finance.service;

import com.guarantee.finance.dto.CfcaSignDTO;
import java.util.List;
import java.util.Map;

public interface CfcaService {

    List<Map<String, Object>> listCertificates();

    boolean signPaymentData(CfcaSignDTO dto);

    boolean verifySignature(String data, String signature);

    Map<String, Object> checkCertificateExpiry();

    void refreshExpiryAlert();
}
