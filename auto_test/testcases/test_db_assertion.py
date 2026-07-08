"""数据库断言单元测试（mock DB，不连真实 MySQL）。"""
from unittest.mock import MagicMock, patch

import pytest

from utils.assertion import run_validations_on_mapping
from utils.db_assertion import run_db_validations, run_teardown
from utils.variable_pool import VariablePool


class TestRunValidationsOnMapping:
    def test_eq_pass(self):
        run_validations_on_mapping(
            {"content": "hello", "status": "PENDING"},
            [{"eq": ["content", "hello"]}, {"eq": ["status", "PENDING"]}],
        )

    def test_eq_fail(self):
        with pytest.raises(AssertionError):
            run_validations_on_mapping({"content": "x"}, [{"eq": ["content", "y"]}])


class TestRunDbValidations:
    @patch("utils.db_assertion.get_db_client")
    def test_query_and_assert(self, mock_get_client):
        mock_client = MagicMock()
        mock_client.query_one.return_value = {
            "content": "自动化测试发帖",
            "status": "PENDING",
        }
        mock_get_client.return_value = mock_client

        pool = VariablePool()
        pool.set("post_id", 1001)
        run_db_validations(
            [
                {
                    "sql": "SELECT content, status FROM hc_post WHERE id = ${post_id}",
                    "expect": [
                        {"eq": ["content", "自动化测试发帖"]},
                        {"eq": ["status", "PENDING"]},
                    ],
                }
            ],
            pool,
        )

        mock_client.query_one.assert_called_once_with(
            "SELECT content, status FROM hc_post WHERE id = 1001"
        )


class TestRunTeardown:
    @patch("utils.db_assertion.get_db_client")
    def test_execute_cleanup_sql(self, mock_get_client):
        mock_client = MagicMock()
        mock_get_client.return_value = mock_client

        pool = VariablePool()
        pool.set("post_id", 88)
        run_teardown(
            [
                {"sql": "DELETE FROM hc_post_image WHERE post_id = ${post_id}"},
                {"sql": "DELETE FROM hc_post WHERE id = ${post_id}"},
            ],
            pool,
        )

        assert mock_client.execute.call_count == 2
        mock_client.execute.assert_any_call("DELETE FROM hc_post_image WHERE post_id = 88")
        mock_client.execute.assert_any_call("DELETE FROM hc_post WHERE id = 88")

    @patch("utils.db_assertion.get_db_client")
    def test_skip_when_variable_missing(self, mock_get_client):
        mock_client = MagicMock()
        mock_get_client.return_value = mock_client

        run_teardown([{"sql": "DELETE FROM hc_post WHERE id = ${post_id}"}], VariablePool())
        mock_client.execute.assert_not_called()
