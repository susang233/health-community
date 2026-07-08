"""
用户模块集成测试（登录、发帖等）。

用例数据在 cases/user/，本文件只负责调度。
"""
import pytest

from utils.loader import load_case, load_cases


pytestmark = pytest.mark.integration


class TestUserApi:
    def test_login_success(self, backend_available, case_runner):
        case_runner.run(load_case("cases/user/login_success.yaml"))

    @pytest.mark.parametrize(
        "case",
        load_cases("cases/user/login_fail.yaml"),
        ids=lambda c: c["case_id"],
    )
    def test_login_fail(self, backend_available, case_runner, case):
        case_runner.run(case)

    def test_login_post_flow(self, backend_available, db_available, case_runner):
        case_runner.run(load_case("cases/user/login_post_flow.yaml"))
