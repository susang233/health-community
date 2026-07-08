"""第一步验收：验证 YAML 用例能被正确加载与校验。"""
import pytest

from utils.loader import load_case, load_cases, list_case_files


class TestCaseLoader:
    def test_load_login_success(self):
        case = load_case("cases/user/login_success.yaml")
        assert case["case_id"] == "user_login_success"
        assert len(case["steps"]) == 1
        step = case["steps"][0]
        assert step["request"]["url"] == "/user/login"
        assert "extract" in step
        assert step["extract"]["token"] == "body.data.token"

    def test_load_login_fail_cases(self):
        cases = load_cases("cases/user/login_fail.yaml")
        assert len(cases) == 2
        assert cases[0]["case_id"] == "user_login_wrong_password"

    def test_list_case_files_skips_template(self):
        files = list_case_files()
        names = [f.name for f in files]
        assert "login_success.yaml" in names
        assert "template.example.yaml" not in names


@pytest.mark.parametrize(
    "case",
    load_cases("cases/user/login_fail.yaml"),
    ids=lambda c: c["case_id"],
)
def test_fail_cases_parametrize_ready(case):
    """验证失败场景 YAML 可直接用于后续 pytest 参数化。"""
    assert case["steps"][0]["request"]["method"] == "POST"
