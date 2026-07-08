"""
食物 / 饮食记录模块集成测试。

用例数据在 cases/food/，新增场景主要加 YAML。
"""
import pytest

from utils.loader import load_case


pytestmark = pytest.mark.integration


class TestFoodApi:
    def test_search_food_flow(self, backend_available, case_runner):
        case_runner.run(load_case("cases/food/search_food_flow.yaml"))

    def test_daily_summary(self, backend_available, case_runner):
        case_runner.run(load_case("cases/food/daily_summary.yaml"))
