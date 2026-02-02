# Claude 到 iFlow 配置同步脚本
# 使用方法：powershell -ExecutionPolicy Bypass -File sync-claude-to-iflow.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Claude 到 iFlow 配置同步工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 同步 Skills
Write-Host "正在同步 Skills..." -ForegroundColor Cyan
if (!(Test-Path 'C:\Users\dreame\.iflow\skills')) { 
    New-Item -ItemType Directory -Path 'C:\Users\dreame\.iflow\skills' -Force | Out-Null
}
Copy-Item 'C:\Users\dreame\.claude\skills\*' 'C:\Users\dreame\.iflow\skills\' -Recurse -Force
$skillCount = (Get-ChildItem 'C:\Users\dreame\.iflow\skills' -Directory).Count
Write-Host "✅ Skills 已同步 ($skillCount 个)" -ForegroundColor Green

# 同步 Agents
Write-Host "正在同步 Agents..." -ForegroundColor Cyan
if (!(Test-Path 'C:\Users\dreame\.iflow\agents')) { 
    New-Item -ItemType Directory -Path 'C:\Users\dreame\.iflow\agents' -Force | Out-Null
}
Copy-Item 'C:\Users\dreame\.claude\agents\*.md' 'C:\Users\dreame\.iflow\agents\' -Force
$agentCount = (Get-ChildItem 'C:\Users\dreame\.iflow\agents' -Filter '*.md').Count
Write-Host "✅ Agents 已同步 ($agentCount 个)" -ForegroundColor Green

# 同步 Commands
Write-Host "正在同步 Commands..." -ForegroundColor Cyan
if (!(Test-Path 'C:\Users\dreame\.iflow\commands')) { 
    New-Item -ItemType Directory -Path 'C:\Users\dreame\.iflow\commands' -Force | Out-Null
}
Copy-Item 'C:\Users\dreame\.claude\commands\*.md' 'C:\Users\dreame\.iflow\commands\' -Force
$commandCount = (Get-ChildItem 'C:\Users\dreame\.iflow\commands' -Filter '*.md').Count
Write-Host "✅ Commands 已同步 ($commandCount 个)" -ForegroundColor Green

# 同步 Rules
Write-Host "正在同步 Rules..." -ForegroundColor Cyan
if (!(Test-Path 'C:\Users\dreame\.iflow\rules')) { 
    New-Item -ItemType Directory -Path 'C:\Users\dreame\.iflow\rules' -Force | Out-Null
}
Copy-Item 'C:\Users\dreame\.claude\rules\*.md' 'C:\Users\dreame\.iflow\rules\' -Force
$ruleCount = (Get-ChildItem 'C:\Users\dreame\.iflow\rules' -Filter '*.md').Count
Write-Host "✅ Rules 已同步 ($ruleCount 个)" -ForegroundColor Green

# 同步 Contexts
Write-Host "正在同步 Contexts..." -ForegroundColor Cyan
if (!(Test-Path 'C:\Users\dreame\.iflow\contexts')) { 
    New-Item -ItemType Directory -Path 'C:\Users\dreame\.iflow\contexts' -Force | Out-Null
}
Copy-Item 'C:\Users\dreame\.claude\contexts\*.md' 'C:\Users\dreame\.iflow\contexts\' -Force
$contextCount = (Get-ChildItem 'C:\Users\dreame\.iflow\contexts' -Filter '*.md').Count
Write-Host "✅ Contexts 已同步 ($contextCount 个)" -ForegroundColor Green

# 更新 AGENTS.md
Write-Host "正在更新 AGENTS.md..." -ForegroundColor Cyan
$skillsList = (Get-ChildItem 'C:\Users\dreame\.iflow\skills' -Directory | ForEach-Object { 
    $name = $_.Name
    "- $name"
}) -join "`n"

$agentsList = (Get-ChildItem 'C:\Users\dreame\.iflow\agents' -Filter '*.md' | ForEach-Object { 
    $name = $_.BaseName
    "- $name"
}) -join "`n"

$commandsList = (Get-ChildItem 'C:\Users\dreame\.iflow\commands' -Filter '*.md' | ForEach-Object { 
    $name = $_.BaseName
    "- $name"
}) -join "`n"

$rulesList = (Get-ChildItem 'C:\Users\dreame\.iflow\rules' -Filter '*.md' | ForEach-Object { 
    $name = $_.BaseName
    "- $name"
}) -join "`n"

$contextsList = (Get-ChildItem 'C:\Users\dreame\.iflow\contexts' -Filter '*.md' | ForEach-Object { 
    $name = $_.BaseName
    "- $name"
}) -join "`n"

$content = @"
## iFlow 全局配置

### Skills ($skillCount 个)
$skillsList

### Agents ($agentCount 个)
$agentsList

### Commands ($commandCount 个)
$commandsList

### Rules ($ruleCount 个)
$rulesList

### Contexts ($contextCount 个)
$contextsList

### 核心编码规范
1. 不可变性 - 永远创建新对象，绝不直接修改
2. 小文件优于大文件 - 200-400行，最多800行
3. 函数要小 - <50行
4. 避免深层嵌套 - 最多4层
5. 完整的错误处理
6. 输入验证
7. 移除console.log
8. 使用typescript类型，避免any
9. 代码质量检查清单

### 推荐工作流
1. 使用/plan进行任务规划
2. 编写代码时遵循coding-standards规范
3. 完成后使用/code-review进行审查
4. 使用/verify验证构建
5. 遇到问题使用/build-fix
6. 需要重构使用/refactor-clean

### 配置位置
- 全局配置: C:\Users\dreame\.iflow
- Skills: C:\Users\dreame\.iflow\skills
- Agents: C:\Users\dreame\.iflow\agents
- Commands: C:\Users\dreame\.iflow\commands
- Rules: C:\Users\dreame\.iflow\rules
- Contexts: C:\Users\dreame\.iflow\contexts

### 最后更新
$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
"@
$content | Out-File -FilePath 'C:\Users\dreame\.iflow\AGENTS.md' -Encoding UTF8
Write-Host "✅ AGENTS.md 已更新" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  同步完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "   - Skills: $skillCount 个" -ForegroundColor Cyan
Write-Host "   - Agents: $agentCount 个" -ForegroundColor Cyan
Write-Host "   - Commands: $commandCount 个" -ForegroundColor Cyan
Write-Host "   - Rules: $ruleCount 个" -ForegroundColor Cyan
Write-Host "   - Contexts: $contextCount 个" -ForegroundColor Cyan
Write-Host ""
Write-Host "重启新项目即可看到所有配置！" -ForegroundColor Yellow