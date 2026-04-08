package com.guarantee.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.guarantee.finance.entity.AccVoucher;
import com.guarantee.finance.entity.AccVoucherDetail;
import com.guarantee.finance.entity.AccVoucherTemplate;
import com.guarantee.finance.entity.AccCustomerSetting;

@Mapper
public interface AccVoucherMapper extends BaseMapper<AccVoucher> {}
@Mapper
public interface AccVoucherDetailMapper extends BaseMapper<AccVoucherDetail> {}
@Mapper
public interface AccVoucherTemplateMapper extends BaseMapper<AccVoucherTemplate> {}
@Mapper
public interface AccCustomerSettingMapper extends BaseMapper<AccCustomerSetting> {}
