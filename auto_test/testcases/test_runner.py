"""
Runner 单元测试（不依赖真实 backend，用 mock 拦截 HTTP）。
"""
from unittest.mock import patch

from core.runner import CaseRunner
from testcases.test_assertion_extract import LOGIN_SUCCESS_BODY, MockResponse
from utils.loader import load_case


class TestCaseRunnerUnit:
    @patch("core.runner.send_request")
    def test_run_login_success_step(self, mock_send, monkeypatch):
        monkeypatch.setenv("USER_USERNAME", "testuser")
        monkeypatch.setenv("USER_PASSWORD", "Password1")

        mock_send.return_value = MockResponse(200, LOGIN_SUCCESS_BODY)

        case = load_case("cases/user/login_success.yaml")
        pool = CaseRunner().run(case)

        assert mock_send.called
        assert pool.get("token") == LOGIN_SUCCESS_BODY["data"]["token"]
        assert pool.get("user_id") == LOGIN_SUCCESS_BODY["data"]["userId"]

        # 检查发给 mock 的 request 已渲染占位符
        sent_request = mock_send.call_args[0][0]
        assert sent_request["json"]["username"] == "testuser"
