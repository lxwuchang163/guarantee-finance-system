SET NAMES utf8mb4;

INSERT INTO acc_voucher_rule (rule_code, rule_name, business_type, business_subtype, voucher_type, summary_template, priority, status) VALUES
('RCPT_PREMIUM', '保费收入规则', 'receipt', '1', 2, '收款单 {businessNo} - {customerName}', 10, 1),
('RCPT_SHARE', '分担收入规则', 'receipt', '2', 2, '收款单 {businessNo} - {customerName}', 10, 1),
('RCPT_RECOVERY', '追偿到款规则', 'receipt', '3', 2, '收款单 {businessNo} - {customerName}', 10, 1),
('PAY_REFUND', '退费支出规则', 'payment', '1', 3, '付款单 {businessNo} - {customerName}', 10, 1),
('PAY_COMPENSATION', '代偿支出规则', 'payment', '2', 3, '付款单 {businessNo} - {customerName}', 10, 1),
('PAY_RECOVERY_DIST', '追回分配规则', 'payment', '3', 3, '付款单 {businessNo} - {customerName}', 10, 1);

INSERT INTO acc_voucher_rule_entry (rule_id, line_no, entry_side, subject_source, subject_code, amount_source, amount_field, summary_template) VALUES
(1, 1, 'debit', 'fixed', '1002', 'field', 'amount', '收款 - {customerName}'),
(1, 2, 'credit', 'fixed', '6001', 'field', 'amount', '收款 - {customerName}'),
(2, 1, 'debit', 'fixed', '1002', 'field', 'amount', '收款 - {customerName}'),
(2, 2, 'credit', 'fixed', '6002', 'field', 'amount', '收款 - {customerName}'),
(3, 1, 'debit', 'fixed', '1002', 'field', 'amount', '收款 - {customerName}'),
(3, 2, 'credit', 'fixed', '6003', 'field', 'amount', '收款 - {customerName}'),
(4, 1, 'debit', 'fixed', '6401', 'field', 'amount', '付款 - {customerName}'),
(4, 2, 'credit', 'fixed', '1002', 'field', 'amount', '付款 - {customerName}'),
(5, 1, 'debit', 'fixed', '6402', 'field', 'amount', '付款 - {customerName}'),
(5, 2, 'credit', 'fixed', '1002', 'field', 'amount', '付款 - {customerName}'),
(6, 1, 'debit', 'fixed', '6403', 'field', 'amount', '付款 - {customerName}'),
(6, 2, 'credit', 'fixed', '1002', 'field', 'amount', '付款 - {customerName}');

INSERT INTO acc_period (period_code, period_name, year, month, start_date, end_date, status, is_current) VALUES
('202601', '2026年1月', 2026, 1, '2026-01-01', '2026-01-31', 'CLOSED', 0),
('202602', '2026年2月', 2026, 2, '2026-02-01', '2026-02-28', 'CLOSED', 0),
('202603', '2026年3月', 2026, 3, '2026-03-01', '2026-03-31', 'CLOSED', 0),
('202604', '2026年4月', 2026, 4, '2026-04-01', '2026-04-30', 'OPEN', 1),
('202605', '2026年5月', 2026, 5, '2026-05-01', '2026-05-31', 'OPEN', 0),
('202606', '2026年6月', 2026, 6, '2026-06-01', '2026-06-30', 'OPEN', 0),
('202607', '2026年7月', 2026, 7, '2026-07-01', '2026-07-31', 'OPEN', 0),
('202608', '2026年8月', 2026, 8, '2026-08-01', '2026-08-31', 'OPEN', 0),
('202609', '2026年9月', 2026, 9, '2026-09-01', '2026-09-30', 'OPEN', 0),
('202610', '2026年10月', 2026, 10, '2026-10-01', '2026-10-31', 'OPEN', 0),
('202611', '2026年11月', 2026, 11, '2026-11-01', '2026-11-30', 'OPEN', 0),
('202612', '2026年12月', 2026, 12, '2026-12-01', '2026-12-31', 'OPEN', 0);

INSERT INTO acc_report_template (template_code, template_name, report_type, template_content, is_default, status) VALUES
('BS_DEFAULT', '资产负债表(默认)', 'BALANCE_SHEET', '{"rows":[{"label":"资产","subjectType":"1"},{"label":"负债","subjectType":"2"},{"label":"所有者权益","subjectType":"3"}]}', 1, 1),
('IS_DEFAULT', '利润表(默认)', 'INCOME_STATEMENT', '{"rows":[{"label":"营业收入","subjectType":"5","balanceDirection":"2"},{"label":"营业成本","subjectType":"5","balanceDirection":"1"}]}', 1, 1),
('CF_DEFAULT', '现金流量表(默认)', 'CASH_FLOW', '{"rows":[{"label":"经营活动"},{"label":"投资活动"},{"label":"筹资活动"}]}', 1, 1);
