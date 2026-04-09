package com.guarantee.finance.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SubjectBalanceVO {

    private Long id;
    private String subjectCode;
    private String period;
    private BigDecimal beginDebit;
    private BigDecimal beginCredit;
    private BigDecimal currentDebit;
    private BigDecimal currentCredit;
    private BigDecimal endDebit;
    private BigDecimal endCredit;
    private String auxiliaryInfo;
    private Integer year;
    private Integer month;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getBeginDebit() {
        return beginDebit;
    }

    public void setBeginDebit(BigDecimal beginDebit) {
        this.beginDebit = beginDebit;
    }

    public BigDecimal getBeginCredit() {
        return beginCredit;
    }

    public void setBeginCredit(BigDecimal beginCredit) {
        this.beginCredit = beginCredit;
    }

    public BigDecimal getCurrentDebit() {
        return currentDebit;
    }

    public void setCurrentDebit(BigDecimal currentDebit) {
        this.currentDebit = currentDebit;
    }

    public BigDecimal getCurrentCredit() {
        return currentCredit;
    }

    public void setCurrentCredit(BigDecimal currentCredit) {
        this.currentCredit = currentCredit;
    }

    public BigDecimal getEndDebit() {
        return endDebit;
    }

    public void setEndDebit(BigDecimal endDebit) {
        this.endDebit = endDebit;
    }

    public BigDecimal getEndCredit() {
        return endCredit;
    }

    public void setEndCredit(BigDecimal endCredit) {
        this.endCredit = endCredit;
    }

    public String getAuxiliaryInfo() {
        return auxiliaryInfo;
    }

    public void setAuxiliaryInfo(String auxiliaryInfo) {
        this.auxiliaryInfo = auxiliaryInfo;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
