[CmdletBinding()]
param(
    [switch]$ResetExistingDemo,
    [string]$ReportDate,
    [string]$BackupDirectory = (Join-Path $env:USERPROFILE '.codex\backups\web-hotel-mis')
)

$ErrorActionPreference = 'Stop'
if (-not $ResetExistingDemo) { throw 'Dùng -ResetExistingDemo khi chủ repo yêu cầu tạo lại dữ liệu QLKS demo local.' }
$demoContainer = 'web-hotel-mis-sqlserver-1433'
$demoSqlcmd = '/opt/mssql-tools18/bin/sqlcmd'
$demoScripts = @('reset_demo.sql', 'populate_operating_demo.sql', 'verify_operating_demo.sql', 'verify_report_demo.sql')
foreach ($demoScript in $demoScripts) {
    if (-not (Test-Path -LiteralPath (Join-Path $PSScriptRoot $demoScript))) { throw "Thiếu $demoScript" }
}
$demoToday = [TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTimeOffset]::UtcNow, 'SE Asia Standard Time').Date
if (-not $ReportDate) { $ReportDate = $demoToday.AddDays(1).ToString('yyyy-MM-dd') }
$demoReportDay = [datetime]::ParseExact($ReportDate, 'yyyy-MM-dd', [Globalization.CultureInfo]::InvariantCulture)
if ($demoReportDay -lt $demoToday -or $demoReportDay -gt $demoToday.AddDays(7)) {
    throw 'Ngày báo cáo phải từ hôm nay đến bảy ngày tới, dạng yyyy-MM-dd.'
}
$demoState = docker inspect $demoContainer --format '{{.State.Status}}'
if ($LASTEXITCODE -ne 0 -or $demoState -ne 'running') { throw 'SQL Server demo local chưa chạy.' }
$demoPassword = (docker exec $demoContainer printenv MSSQL_SA_PASSWORD).Trim()
if (-not $demoPassword) { throw 'Không lấy được cấu hình kết nối SQL Server demo.' }
function Invoke-ReportDemoSql([string]$Query) {
    $demoResult = docker exec -e "SQLCMDPASSWORD=$demoPassword" $demoContainer $demoSqlcmd -S localhost -U sa -C -d QLKS -b -I -W -Q $Query
    if ($LASTEXITCODE -ne 0) { throw 'Lệnh SQL trên QLKS demo local thất bại.' }
    $demoResult
}
Invoke-ReportDemoSql "IF DB_NAME()<>N'QLKS' OR NOT EXISTS(SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=N'HOUSEKEEP')
    THROW 51000,N'Không đúng database demo local đã biết.',1;
IF (SELECT COUNT(*) FROM dbo.flyway_schema_history WHERE success=1 AND version IN('1','2','3','4','5','6'))<>6
    THROW 51000,N'Database chưa có đủ Flyway V1-V6.',1;" | Out-Null

$demoStamp = [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss') + 'Z'
$demoBackupName = "QLKS-before-report-demo-$demoStamp.bak"
$demoContainerBackup = "/var/opt/mssql/backup/$demoBackupName"
New-Item -ItemType Directory -Path $BackupDirectory -Force | Out-Null
$demoLocalBackup = Join-Path $BackupDirectory $demoBackupName
$demoLog = Join-Path $BackupDirectory "QLKS-report-demo-$demoStamp.log"
$demoWrapperFile = Join-Path ([IO.Path]::GetTempPath()) ("hotel-report-demo-" + [guid]::NewGuid() + '.sql')
docker exec $demoContainer mkdir -p /var/opt/mssql/backup
if ($LASTEXITCODE -ne 0) { throw 'Không tạo được thư mục backup trong container.' }
Invoke-ReportDemoSql "BACKUP DATABASE [QLKS] TO DISK=N'$demoContainerBackup' WITH COPY_ONLY,INIT,CHECKSUM;
    RESTORE VERIFYONLY FROM DISK=N'$demoContainerBackup' WITH CHECKSUM;" | Out-Null
docker cp "${demoContainer}:$demoContainerBackup" $demoLocalBackup
if ($LASTEXITCODE -ne 0) { throw 'Không chép được backup ra máy; chưa thay dữ liệu.' }

# Một transaction ngoài bao cả reset, bổ sung và kiểm tra; sqlcmd ngắt khi lỗi
# sẽ đóng kết nối và rollback. Giữ nguyên hash của các tài khoản demo còn dùng.
$demoWrapper = @"
:on error exit
USE [QLKS];
SET XACT_ABORT ON;
BEGIN TRANSACTION;
-- Khóa bảng được giữ đến COMMIT/ROLLBACK trên chính kết nối này. Không đổi
-- SINGLE_USER: connection pool của backend có thể giành mất suất kết nối đó.
DECLARE @demoTableLocks NVARCHAR(MAX),@demoLockedRows BIGINT;
SELECT @demoTableLocks=STRING_AGG(CAST(N'SELECT @rows=COUNT_BIG(*) FROM '
    +QUOTENAME(SCHEMA_NAME(schema_id))+N'.'+QUOTENAME(name)+N' WITH(TABLOCKX,HOLDLOCK);' AS NVARCHAR(MAX)),CHAR(10))
    WITHIN GROUP(ORDER BY name)
FROM sys.tables WHERE is_ms_shipped=0 AND schema_id=SCHEMA_ID(N'dbo');
EXEC sys.sp_executesql @demoTableLocks,N'@rows BIGINT OUTPUT',@rows=@demoLockedRows OUTPUT;
SELECT maNhanVien,matKhau INTO #ReportEmployeePasswords FROM dbo.NhanVien;
SELECT soDienThoai,matKhau INTO #ReportCustomerPasswords FROM dbo.TaiKhoanKhachHang;
EXEC sys.sp_set_session_context @key=N'demo_report_date',@value=N'$ReportDate';
GO
:r /tmp/reset_demo.sql
GO
UPDATE e SET matKhau=p.matKhau FROM dbo.NhanVien e JOIN #ReportEmployeePasswords p ON p.maNhanVien=e.maNhanVien;
UPDATE c SET matKhau=p.matKhau FROM dbo.TaiKhoanKhachHang c JOIN #ReportCustomerPasswords p ON p.soDienThoai=c.soDienThoai;
GO
:r /tmp/populate_operating_demo.sql
GO
UPDATE c SET matKhau=p.matKhau FROM dbo.TaiKhoanKhachHang c JOIN #ReportCustomerPasswords p ON p.soDienThoai=c.soDienThoai;
:r /tmp/verify_operating_demo.sql
GO
:r /tmp/verify_report_demo.sql
GO
IF @@TRANCOUNT<>1 THROW 51000,N'Transaction demo không còn nguyên vẹn.',1;
COMMIT;
PRINT N'COMMITTED: report demo verified.';
GO
"@
try {
    foreach ($demoScript in $demoScripts) {
        docker cp (Join-Path $PSScriptRoot $demoScript) "${demoContainer}:/tmp/$demoScript"
        if ($LASTEXITCODE -ne 0) { throw "Không sao chép được $demoScript" }
    }
    [IO.File]::WriteAllText($demoWrapperFile, $demoWrapper, (New-Object Text.UTF8Encoding($false)))
    docker cp $demoWrapperFile "${demoContainer}:/tmp/refresh_report_demo.sql"
    if ($LASTEXITCODE -ne 0) { throw 'Không sao chép được lệnh nạp demo.' }
    $demoOutput = docker exec -e "SQLCMDPASSWORD=$demoPassword" $demoContainer $demoSqlcmd -S localhost -U sa -C -d QLKS -b -I -i /tmp/refresh_report_demo.sql
    $demoExit = $LASTEXITCODE
    [IO.File]::WriteAllLines($demoLog, [string[]]$demoOutput, (New-Object Text.UTF8Encoding($false)))
    $demoOutput | Select-Object -Last 35 | Write-Output
    if ($demoExit -ne 0) { throw "Nạp demo lỗi, transaction đã rollback. Backup: $demoLocalBackup. Log: $demoLog" }
    Write-Output "Đã tạo lại dữ liệu cho ngày báo cáo $ReportDate. Backup: $demoLocalBackup. Log: $demoLog"
}
finally {
    if (Test-Path -LiteralPath $demoWrapperFile) { Remove-Item -LiteralPath $demoWrapperFile }
}
