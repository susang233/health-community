"""
帖子模块集成测试（含文件上传）。

依赖:
  - backend 已启动
  - MinIO 可用（dev 环境上传图片）
"""
import pytest

from utils.loader import load_case

pytestmark = pytest.mark.integration


class TestPostApi:
    def test_upload_post_images(self, backend_available, case_runner):
        case_runner.run(load_case("cases/post/upload_post_images.yaml"))

    def test_upload_and_create_post(self, backend_available, case_runner):
        case_runner.run(load_case("cases/post/upload_and_create_post.yaml"))
