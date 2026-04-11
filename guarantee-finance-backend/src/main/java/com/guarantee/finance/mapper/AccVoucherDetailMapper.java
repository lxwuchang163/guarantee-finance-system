package com.guarantee.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.guarantee.finance.entity.AccVoucherDetail;
import com.guarantee.finance.vo.VoucherDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccVoucherDetailMapper extends BaseMapper<AccVoucherDetail> {

    @Select("SELECT * FROM acc_voucher_detail WHERE voucher_id = #{voucherId} AND deleted = 0 ORDER BY line_no")
    List<VoucherDetailVO> selectByVoucherId(@Param("voucherId") Long voucherId);
}
