package com.guarantee.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guarantee.finance.entity.BankTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BankTransactionMapper extends BaseMapper<BankTransaction> {
}
