"""
用例执行引擎（Case Runner）
"""
from __future__ import annotations

import json
from typing import Any

from config.setting import config
from utils.assertion import run_validations
from utils.db_assertion import run_db_validations, run_teardown
from utils.extract import extract_vars
from utils.http_client import send_request
from utils.render import render, render_step_request
from utils.variable_pool import VariablePool

# Allure 是可选依赖：装了就能在报告里看到分步和请求响应附件
try:
    import allure

    ALLURE_AVAILABLE = True
except ImportError:  # pragma: no cover
    ALLURE_AVAILABLE = False


class CaseRunner:
    """
    单条 YAML 用例的执行器。

    用法:
        runner = CaseRunner()
        case = load_case("cases/user/login_success.yaml")
        runner.run(case)
    """

    def run(self, case: dict) -> VariablePool:
        """
        执行一条完整用例（包含一个或多个 step）。

        参数:
            case: loader 读出来的用例 dict，必须有 steps 列表

        返回:
            执行结束后的变量池（里面可能有 token、user_id 等，方便调试）

        抛出:
            AssertionError: 断言失败
            requests 相关异常: 网络错误、连接被拒绝（backend 未启动）
        """
        # 每条用例使用独立变量池，避免用例之间 token 互相污染
        pool = VariablePool()
        pool.load_env()

        case_name = case.get("name") or case.get("case_id") or "未命名用例"
        steps = case["steps"]

        try:
            for index, step in enumerate(steps):
                step_name = step.get("name") or f"步骤{index + 1}"
                self._run_step(
                    step=step,
                    pool=pool,
                    case_name=case_name,
                    step_index=index,
                    step_name=step_name,
                )
        finally:
            if case.get("teardown") and config.database_enabled:
                run_teardown(render(case["teardown"], pool), pool)

        return pool

    def _run_step(
        self,
        step: dict,
        pool: VariablePool,
        case_name: str,
        step_index: int,
        step_name: str,
    ) -> None:
        """
        执行单个 step（内部方法）。

        参数:
            step      : YAML 里的一个 step
            pool      : 当前用例的变量池（会跨 step 传递 extract 结果）
            case_name : 用例名，仅用于以后打日志 / Allure 报告
            step_index: 步骤序号，从 0 开始
            step_name : 步骤名
        """
        step_label = f"{case_name} → {step_name}"

        if ALLURE_AVAILABLE:
            # allure.step 会在 Allure 报告里展示为可展开的步骤
            with allure.step(step_label):
                self._execute_step(step, pool)
        else:
            self._execute_step(step, pool)

    def _execute_step(self, step: dict, pool: VariablePool) -> None:
        """真正执行单步流水线（render → 请求 → 断言 → 提取）。"""
        rendered_request = render_step_request(step, pool)
        response = send_request(rendered_request)

        raw_validate = step.get("validate") or []
        rendered_validate = render(raw_validate, pool)
        run_validations(response, rendered_validate)

        extract_rules = step.get("extract")
        if extract_rules:
            extract_vars(response, extract_rules, pool)

        db_rules = step.get("db_validate")
        if db_rules:
            if not config.database_enabled:
                raise RuntimeError(
                    f"步骤「{step.get('name', '未命名')}」配置了 db_validate，"
                    "但 database.enabled=false，请在 config 中启用数据库"
                )
            run_db_validations(render(db_rules, pool), pool)

        # 附加请求/响应到 Allure 报告，失败时方便排查
        if ALLURE_AVAILABLE:
            allure.attach(
                json.dumps(rendered_request, ensure_ascii=False, indent=2),
                name="request",
                attachment_type=allure.attachment_type.JSON,
            )
            allure.attach(
                response.text,
                name="response",
                attachment_type=allure.attachment_type.JSON,
            )
