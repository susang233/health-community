"""http_client 单元测试（含 multipart 文件上传）。"""
from pathlib import Path
from unittest.mock import MagicMock, patch

import pytest

from utils.http_client import build_multipart_files, send_request
from utils.loader import ROOT_DIR


class TestBuildMultipartFiles:
    def test_build_single_file(self):
        file_path = ROOT_DIR / "testdata" / "post1.png"
        multipart, handles = build_multipart_files(
            [{"field": "files", "path": "testdata/post1.png"}]
        )
        try:
            assert len(multipart) == 1
            field, (filename, handle, mime) = multipart[0]
            assert field == "files"
            assert filename == "post1.png"
            assert mime == "image/png"
            assert Path(file_path).is_file()
        finally:
            for h in handles:
                h.close()

    def test_missing_file_raises(self):
        with pytest.raises(FileNotFoundError):
            build_multipart_files([{"field": "file", "path": "testdata/not_exist.png"}])


class TestSendRequestWithFiles:
    @patch("utils.http_client.requests.request")
    def test_multipart_removes_json_content_type(self, mock_request):
        mock_request.return_value = MagicMock(status_code=200, text="{}")
        send_request(
            {
                "method": "POST",
                "url": "/user/post/upload-post-images",
                "headers": {"Authorization": "Bearer t"},
                "files": [{"field": "files", "path": "testdata/post1.png"}],
            }
        )
        _, kwargs = mock_request.call_args
        assert "Content-Type" not in kwargs["headers"]
        assert "files" in kwargs
        assert len(kwargs["files"]) == 1
