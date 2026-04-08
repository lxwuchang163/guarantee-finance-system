package com.guarantee.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guarantee.finance.entity.AccVoucherDetail;
import com.guarantee.finance.vo.VoucherDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccVoucherDetailMapper extends BaseMapper<AccVoucherDetail> {

    List<VoucherDetailVO> selectByVoucherId(@Param("voucherId") Long voucherId);

    IPage<VoucherDetailVO> selectPageByVoucherId(@Param("voucherId") Long voucherId, Page<?> page);
}
