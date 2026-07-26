# Codex r14.10 启动包

## 项目谱系

- 最上游功能基线：`MonwF/customiuizer@v24.10.12`
- 当前独立维护仓库：`tomthenpc/customiuizer-a14`
- 当前改造方向：独立包名/版本线 → libxposed API 101 → Kotlin 重构 → API 101/102 单 APK双兼容

Codex 应从当前独立仓库新 clone。上游 tag 只用于功能和历史行为对照，不用于覆盖、merge、rebase 或 reset 当前项目。

包含：

- `forCodeX_r14.10_start.txt`：新开 Codex 会话后发送的任务指令
- `AGENTS.md`：放到项目根目录，作为项目级长期约束
- `docs/ENGINEERING_METHOD.md`：完整性能、稳定性和兼容性方法论
- `docs/KOTLIN_POST_MIGRATION_REVIEW.md`：Kotlin 重构后二次审查规则

## 推荐使用流程

```powershell
cd C:\Users\tv\Downloads\Peengeek
git clone https://github.com/tomthenpc/customiuizer-a14.git customiuizer-a14-codex-r14.10
cd customiuizer-a14-codex-r14.10
git fetch --all --tags --prune
git switch -c r14.10.0-api101-api102 origin/main
```

然后将本启动包中的：

- `AGENTS.md`
- `docs` 目录

复制到新 clone 的项目根目录。

从项目根目录启动 Codex，先让它读取 `AGENTS.md`，再发送
`forCodeX_r14.10_start.txt` 的完整内容。

第一轮建议只允许 Codex在独立本地分支提交，不推送、不合并 main、不创建 Release。
