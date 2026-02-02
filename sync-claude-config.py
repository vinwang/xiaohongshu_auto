#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Claude 配置同步工具
支持将 Claude CLI 的配置同步到其他 CLI 工具（如 iFlow CLI）
"""

import os
import shutil
from pathlib import Path
from datetime import datetime
import json
import argparse


class ClaudeConfigSync:
    """Claude 配置同步器"""
    
    def __init__(self, source_cli="claude", target_cli="iflow"):
        self.user_home = Path.home()
        self.source_cli = source_cli
        self.target_cli = target_cli
        
        # 配置路径
        self.source_path = self.user_home / f".{source_cli}"
        self.target_path = self.user_home / f".{target_cli}"
        
        # 需要同步的目录
        self.sync_dirs = [
            "skills",
            "agents", 
            "commands",
            "rules",
            "contexts"
        ]
        
        # 统计信息
        self.stats = {}
    
    def check_source_exists(self):
        """检查源配置是否存在"""
        if not self.source_path.exists():
            print(f"❌ 源配置目录不存在: {self.source_path}")
            return False
        return True
    
    def create_target_dirs(self):
        """创建目标目录"""
        for dir_name in self.sync_dirs:
            target_dir = self.target_path / dir_name
            target_dir.mkdir(parents=True, exist_ok=True)
    
    def sync_directory(self, dir_name):
        """同步单个目录"""
        source_dir = self.source_path / dir_name
        target_dir = self.target_path / dir_name
        
        if not source_dir.exists():
            print(f"⚠️  源目录不存在，跳过: {dir_name}")
            return 0
        
        # 统计同步的文件数量
        count = 0
        
        if dir_name == "skills":
            # Skills 是目录结构
            for item in source_dir.iterdir():
                if item.is_dir():
                    target_item = target_dir / item.name
                    if target_item.exists():
                        shutil.rmtree(target_item)
                    shutil.copytree(item, target_item)
                    count += 1
        else:
            # 其他目录是文件
            for item in source_dir.glob("*.md"):
                target_item = target_dir / item.name
                shutil.copy2(item, target_item)
                count += 1
        
        return count
    
    def update_agents_md(self):
        """更新 AGENTS.md 文件"""
        content = ["## 全局配置清单"]
        content.append(f"\n### 来源: {self.source_cli}")
        content.append(f"### 目标: {self.target_cli}")
        content.append(f"### 同步时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # 统计各类配置
        for dir_name in self.sync_dirs:
            dir_path = self.target_path / dir_name
            
            if dir_name == "skills":
                items = [d.name for d in dir_path.iterdir() if d.is_dir()]
            else:
                items = [f.stem for f in dir_path.glob("*.md")]
            
            if items:
                content.append(f"\n### {dir_name.capitalize()} ({len(items)} 个)")
                for item in sorted(items):
                    content.append(f"- {item}")
        
        # 添加编码规范
        content.append("\n### 核心编码规范")
        content.append("1. 不可变性 - 永远创建新对象，绝不直接修改")
        content.append("2. 小文件优于大文件 - 200-400行，最多800行")
        content.append("3. 函数要小 - <50行")
        content.append("4. 避免深层嵌套 - 最多4层")
        content.append("5. 完整的错误处理")
        content.append("6. 输入验证")
        content.append("7. 移除console.log")
        content.append("8. 使用typescript类型，避免any")
        content.append("9. 代码质量检查清单")
        
        # 添加配置位置
        content.append("\n### 配置位置")
        content.append(f"- 源配置: {self.source_path}")
        content.append(f"- 目标配置: {self.target_path}")
        
        # 写入文件
        agents_md_path = self.target_path / "AGENTS.md"
        with open(agents_md_path, 'w', encoding='utf-8') as f:
            f.write('\n'.join(content))
    
    def sync(self):
        """执行同步"""
        print("=" * 50)
        print("  Claude 配置同步工具")
        print("=" * 50)
        print(f"  源: {self.source_cli} ({self.source_path})")
        print(f"  目标: {self.target_cli} ({self.target_path})")
        print("=" * 50)
        print()
        
        # 检查源配置
        if not self.check_source_exists():
            return False
        
        # 创建目标目录
        self.create_target_dirs()
        
        # 同步各个目录
        print("开始同步...")
        print()
        
        total_count = 0
        for dir_name in self.sync_dirs:
            print(f"正在同步 {dir_name}...", end=" ")
            count = self.sync_directory(dir_name)
            self.stats[dir_name] = count
            total_count += count
            print(f"✅ {count} 个")
        
        # 更新 AGENTS.md
        print("正在更新 AGENTS.md...", end=" ")
        self.update_agents_md()
        print("✅")
        
        # 显示统计信息
        print()
        print("=" * 50)
        print("  同步完成！")
        print("=" * 50)
        print()
        for dir_name, count in self.stats.items():
            print(f"  - {dir_name.capitalize()}: {count} 个")
        print(f"  - 总计: {total_count} 个")
        print()
        print("重启新项目即可看到所有配置！")
        
        return True


def main():
    """主函数"""
    parser = argparse.ArgumentParser(
        description="Claude 配置同步工具",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
使用示例:
  # 同步到 iFlow CLI
  python sync-claude-config.py --target iflow
  
  # 同步到自定义 CLI
  python sync-claude-config.py --target mycli
  
  # 从自定义源同步
  python sync-claude-config.py --source claude --target iflow
  
  # 只显示统计信息，不实际同步
  python sync-claude-config.py --dry-run
        """
    )
    
    parser.add_argument(
        "--source",
        default="claude",
        help="源 CLI 名称 (默认: claude)"
    )
    
    parser.add_argument(
        "--target",
        default="iflow",
        help="目标 CLI 名称 (默认: iflow)"
    )
    
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="只显示统计信息，不实际同步"
    )
    
    args = parser.parse_args()
    
    # 创建同步器
    syncer = ClaudeConfigSync(
        source_cli=args.source,
        target_cli=args.target
    )
    
    if args.dry_run:
        # 只显示统计信息
        print(f"源配置: {syncer.source_path}")
        print(f"目标配置: {syncer.target_path}")
        print()
        print("将要同步的目录:")
        for dir_name in syncer.sync_dirs:
            source_dir = syncer.source_path / dir_name
            if dir_name == "skills":
                count = len([d for d in source_dir.iterdir() if d.is_dir()])
            else:
                count = len(list(source_dir.glob("*.md")))
            print(f"  - {dir_name}: {count} 个")
    else:
        # 执行同步
        syncer.sync()


if __name__ == "__main__":
    main()