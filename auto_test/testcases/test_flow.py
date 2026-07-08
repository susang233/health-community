# 第五步验收：串联用例 + Allure 步骤（mock HTTP，不依赖 backend）
from unittest.mock import patch

from core.runner import CaseRunner
from testcases.test_assertion_extract import LOGIN_SUCCESS_BODY, MockResponse
from utils.loader import load_case


class TestLoginPostFlowUnit:
    @patch("core.runner.run_teardown")
    @patch("core.runner.run_db_validations")
    @patch("core.runner.send_request")
    def test_login_post_flow_mock(self, mock_send, mock_db_validate, mock_teardown, monkeypatch):
        monkeypatch.setenv("USER_USERNAME", "testuser")
        monkeypatch.setenv("USER_PASSWORD", "Password1!")

        post_id = 42
        mock_send.side_effect = [
            MockResponse(200, LOGIN_SUCCESS_BODY),
            MockResponse(200, {"code": 200, "message": "操作成功", "data": post_id}),
        ]

        pool = CaseRunner().run(load_case("cases/user/login_post_flow.yaml"))

        assert mock_send.call_count == 2
        assert pool.get("token") == LOGIN_SUCCESS_BODY["data"]["token"]
        assert pool.get("post_id") == post_id

        # 第二步请求应带上 Bearer token
        second_request = mock_send.call_args_list[1][0][0]
        assert "Bearer" in second_request["headers"]["Authorization"]
