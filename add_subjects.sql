-- 插入资产类科目
INSERT INTO acc_account_subject (subject_code, subject_name, subject_level, parent_code, subject_type, balance_direction, status, description, sort_order, category, system_type)
VALUES
('1201', '应收担保费', 1, null, 1, 1, 1, '核算应收未收的担保费', 1, '资产', '0'),
('1231', '应收代偿款', 1, null, 1, 1, 1, '核算代被担保人清偿后应收的款项', 2, '资产', '0'),
('1241', '应收担保损失补贴款', 1, null, 1, 1, 1, '核算应收的担保损失补贴', 3, '资产', '0'),
('1311', '存出担保保证金', 1, null, 1, 1, 1, '核算企业存出的担保保证金', 4, '资产', '0'),
('1261', '坏账准备', 1, null, 1, 2, 1, '核算应收款项的坏账准备', 5, '资产（备抵）', '0'),
('1911', '抵债资产', 1, null, 1, 1, 1, '核算收到的抵债资产', 6, '资产', '0'),
('1921', '抵债资产减值准备', 1, null, 1, 2, 1, '抵债资产的减值准备', 7, '资产（备抵）', '0');

-- 插入负债类科目
INSERT INTO acc_account_subject (subject_code, subject_name, subject_level, parent_code, subject_type, balance_direction, status, description, sort_order, category, system_type)
VALUES
('2131', '预收担保费', 1, null, 2, 2, 1, '核算预收的担保费', 1, '负债', '0'),
('2141', '存入担保保证金', 1, null, 2, 2, 1, '核算按合同规定收到的担保履约保证金', 2, '负债', '0'),
('2192', '担保赔偿准备', 1, null, 2, 2, 1, '核算按规定提取的担保赔偿准备', 3, '负债', '0'),
('2193', '未到期责任准备', 1, null, 2, 2, 1, '核算提取的未到期责任准备', 4, '负债', '0');

-- 插入损益类科目
INSERT INTO acc_account_subject (subject_code, subject_name, subject_level, parent_code, subject_type, balance_direction, status, description, sort_order, category, system_type)
VALUES
('4101', '担保费收入', 1, null, 5, 2, 1, '核算担保费收入', 1, '收入', '0'),
('4105', '追偿收入', 1, null, 5, 2, 1, '核算追偿所得超过代偿款的差额', 2, '收入', '0'),
('4401', '担保赔偿支出', 1, null, 5, 1, 1, '核算担保赔偿损失', 3, '支出', '0'),
('4501', '营业费用', 1, null, 5, 1, 1, '核算计提准备金等费用', 4, '费用', '0'),
('4701', '资产减值损失', 1, null, 5, 1, 1, '核算资产减值损失', 5, '损失', '0');

-- 插入所有者权益类科目
INSERT INTO acc_account_subject (subject_code, subject_name, subject_level, parent_code, subject_type, balance_direction, status, description, sort_order, category, system_type)
VALUES
('3121', '一般风险准备', 1, null, 3, 2, 1, '从净利润中提取的风险准备金', 1, '权益', '0'),
('3151', '担保扶持基金', 1, null, 3, 2, 1, '收到的政策性担保扶持基金', 2, '权益', '0');

-- 插入表外科目
INSERT INTO acc_account_subject (subject_code, subject_name, subject_level, parent_code, subject_type, balance_direction, status, description, sort_order, category, system_type)
VALUES
('5101', '代管担保基金', 1, null, 6, 0, 1, '核算受托代为管理的担保基金，专户存储', 1, '表外科目', '0');