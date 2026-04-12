# 设置UTF-8编码
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 从文件读取JSON数据
$jsonContent = Get-Content -Path "C:\Users\Administrator\Desktop\Business-Finance System\subjects.json" -Encoding UTF8 | ConvertFrom-Json

# API请求的基础信息
$token = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJzdWIiOiJhZG1pbiIsImlhdCI6MTc3NTkxNzY1NiwiZXhwIjoxNzc2MDA0MDU2fQ.rjAp8nAP2VSiBjjFvNO1tyzmsD2c0P2kGPoUC23i60RTYNunMCmFeYuEOZdTAF7vhm38eqodxiff9AATKIwkKQ"
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# 遍历并添加每个科目
foreach ($subject in $jsonContent) {
    $body = $subject | ConvertTo-Json -Depth 10
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/accounting/subject" -Method POST -Headers $headers -Body $body
        Write-Output "成功添加科目: $($subject.subjectCode) - $($subject.subjectName)"
    } catch {
        Write-Output "添加科目失败: $($subject.subjectCode) - $($subject.subjectName)"
        Write-Output "错误信息: $($_.Exception.Message)"
    }
}

Write-Output "科目添加完成！"