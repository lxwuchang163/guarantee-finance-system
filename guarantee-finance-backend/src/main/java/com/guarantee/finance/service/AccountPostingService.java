package com.guarantee.finance.service;

public interface AccountPostingService {

    void postVoucher(Long voucherId);

    void unpostVoucher(Long voucherId);
}
