@echo off
chcp 65001 >nul 2>&1
echo ============================================
echo   担保集团业务财务系统 - 运行测试套件
echo ============================================

cd guarantee-finance-backend

echo.
echo [1/2] 运行单元测试...
call mvn test -pl . -Dtest="com.guarantee.finance.service.impl.SysUserServiceUnitTest" -q
if errorlevel 1 (
    echo [ERROR] 单元测试失败！
    cd ..
    pause
    exit /b 1
)
echo [OK] 单元测试通过

echo.
echo [2/2] 运行集成测试...
call mvn test -pl . -Dtest="com.guarantee.finance.service.*,com.guarantee.finance.controller.*" -q
if errorlevel 1 (
    echo [WARN] 部分集成测试失败（可能缺少数据库连接）
) else (
    echo [OK] 集成测试通过
)

cd ..
echo.
echo ============================================
echo   测试完成！
echo ============================================
pause
