import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddSubjects {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/guarantee_finance?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false";
        String username = "root";
        String password = "123";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            // 资产类科目
            addSubject(conn, "1201", "应收担保费", 1, null, 1, 1, 1, "核算应收未收的担保费", 1, "资产", "0");
            addSubject(conn, "1231", "应收代偿款", 1, null, 1, 1, 1, "核算代被担保人清偿后应收的款项", 2, "资产", "0");
            addSubject(conn, "1241", "应收担保损失补贴款", 1, null, 1, 1, 1, "核算应收的担保损失补贴", 3, "资产", "0");
            addSubject(conn, "1311", "存出担保保证金", 1, null, 1, 1, 1, "核算企业存出的担保保证金", 4, "资产", "0");
            addSubject(conn, "1261", "坏账准备", 1, null, 1, 2, 1, "核算应收款项的坏账准备", 5, "资产（备抵）", "0");
            addSubject(conn, "1911", "抵债资产", 1, null, 1, 1, 1, "核算收到的抵债资产", 6, "资产", "0");
            addSubject(conn, "1921", "抵债资产减值准备", 1, null, 1, 2, 1, "抵债资产的减值准备", 7, "资产（备抵）", "0");

            // 负债类科目
            addSubject(conn, "2131", "预收担保费", 1, null, 2, 2, 1, "核算预收的担保费", 1, "负债", "0");
            addSubject(conn, "2141", "存入担保保证金", 1, null, 2, 2, 1, "核算按合同规定收到的担保履约保证金", 2, "负债", "0");
            addSubject(conn, "2192", "担保赔偿准备", 1, null, 2, 2, 1, "核算按规定提取的担保赔偿准备", 3, "负债", "0");
            addSubject(conn, "2193", "未到期责任准备", 1, null, 2, 2, 1, "核算提取的未到期责任准备", 4, "负债", "0");

            // 损益类科目
            addSubject(conn, "4101", "担保费收入", 1, null, 5, 2, 1, "核算担保费收入", 1, "收入", "0");
            addSubject(conn, "4105", "追偿收入", 1, null, 5, 2, 1, "核算追偿所得超过代偿款的差额", 2, "收入", "0");
            addSubject(conn, "4401", "担保赔偿支出", 1, null, 5, 1, 1, "核算担保赔偿损失", 3, "支出", "0");
            addSubject(conn, "4501", "营业费用", 1, null, 5, 1, 1, "核算计提准备金等费用", 4, "费用", "0");
            addSubject(conn, "4701", "资产减值损失", 1, null, 5, 1, 1, "核算资产减值损失", 5, "损失", "0");

            // 所有者权益类科目
            addSubject(conn, "3121", "一般风险准备", 1, null, 3, 2, 1, "从净利润中提取的风险准备金", 1, "权益", "0");
            addSubject(conn, "3151", "担保扶持基金", 1, null, 3, 2, 1, "收到的政策性担保扶持基金", 2, "权益", "0");

            // 表外科目
            addSubject(conn, "5101", "代管担保基金", 1, null, 6, 0, 1, "核算受托代为管理的担保基金，专户存储", 1, "表外科目", "0");

            System.out.println("科目添加完成！");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addSubject(Connection conn, String subjectCode, String subjectName, int subjectLevel, String parentCode, int subjectType, int balanceDirection, int status, String description, int sortOrder, String category, String systemType) throws SQLException {
        String sql = "INSERT INTO acc_account_subject (subject_code, subject_name, subject_level, parent_code, subject_type, balance_direction, status, description, sort_order, category, system_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "subject_name = VALUES(subject_name), " +
                "subject_level = VALUES(subject_level), " +
                "parent_code = VALUES(parent_code), " +
                "subject_type = VALUES(subject_type), " +
                "balance_direction = VALUES(balance_direction), " +
                "status = VALUES(status), " +
                "description = VALUES(description), " +
                "sort_order = VALUES(sort_order), " +
                "category = VALUES(category), " +
                "system_type = VALUES(system_type)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subjectCode);
            pstmt.setString(2, subjectName);
            pstmt.setInt(3, subjectLevel);
            pstmt.setString(4, parentCode);
            pstmt.setInt(5, subjectType);
            pstmt.setInt(6, balanceDirection);
            pstmt.setInt(7, status);
            pstmt.setString(8, description);
            pstmt.setInt(9, sortOrder);
            pstmt.setString(10, category);
            pstmt.setString(11, systemType);
            pstmt.executeUpdate();
            System.out.println("添加科目成功: " + subjectCode + " - " + subjectName);
        }
    }
}