package com.guarantee.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.entity.AccVoucherAudit;
import com.guarantee.finance.vo.VoucherAuditVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccVoucherAuditMapper extends BaseMapper<AccVoucherAudit> {

    List<VoucherAuditVO> selectByVoucherId(@Param("voucherId") Long voucherId);

    IPage<VoucherAuditVO> selectAuditPage(@Param("voucherNo") String voucherNo, @Param("auditStatus") String auditStatus, Page<?> page);
}
