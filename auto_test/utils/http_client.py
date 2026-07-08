"""
HTTP 客户端 — 框架对 requests 的统一封装。

支持:
  - json / params  : 常规 JSON 接口
  - files          : multipart 文件上传（multipart/form-data）

上传文件时不要手动设置 Content-Type: application/json，
requests 会在传 files= 时自动带上 multipart 边界。
"""
from __future__ import annotations

import mimetypes
from pathlib import Path
from typing import Any

import requests

from config.setting import config
from utils.loader import resolve_path

# 扩展名 → MIME（后端仅允许图片时够用）
MIME_BY_SUFFIX = {
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".png": "image/png",
    ".gif": "image/gif",
    ".webp": "image/webp",
}


def _guess_mime_type(file_path: Path) -> str:
    """根据扩展名推断 MIME 类型。"""
    mime = MIME_BY_SUFFIX.get(file_path.suffix.lower())
    if mime:
        return mime
    guessed, _ = mimetypes.guess_type(file_path.name)
    return guessed or "application/octet-stream"


def build_multipart_files(files_spec: list[dict]) -> tuple[list[tuple], list]:
    """
    把 YAML 里的 files 配置转成 requests 的 files 参数。

    YAML 示例:
        files:
          - field: files
            path: testdata/post1.png
          - field: files
            path: testdata/post2.png

    返回:
        (multipart, opened_handles)
        opened_handles 用于请求结束后关闭文件句柄
    """
    if not files_spec:
        raise ValueError("files 配置不能为空")

    multipart: list[tuple] = []
    opened_handles: list = []

    for index, item in enumerate(files_spec):
        if not isinstance(item, dict):
            raise ValueError(f"files[{index}] 必须是 dict")
        field = item.get("field")
        path_value = item.get("path")
        if not field or not path_value:
            raise ValueError(f"files[{index}] 必须包含 field 和 path")

        file_path = resolve_path(str(path_value))
        if not file_path.is_file():
            raise FileNotFoundError(f"上传文件不存在: {file_path}")

        handle = open(file_path, "rb")
        opened_handles.append(handle)
        multipart.append(
            (
                str(field),
                (file_path.name, handle, _guess_mime_type(file_path)),
            )
        )

    return multipart, opened_handles


def send_request(request_data: dict) -> requests.Response:
    """
    根据渲染后的 request 字典发送 HTTP 请求。

    request_data 可包含:
      - method, url, headers
      - json   : JSON 请求体
      - params : URL 查询参数
      - files  : 文件上传列表 [{field, path}, ...]
    """
    method = request_data["method"].upper()
    url = request_data["url"]
    files_spec = request_data.get("files")

    headers = {**config.default_headers, **request_data.get("headers", {})}

    # multipart 上传时去掉 JSON 的 Content-Type，让 requests 自动设置
    if files_spec:
        headers.pop("Content-Type", None)

    kwargs: dict = {
        "timeout": config.timeout,
        "headers": headers,
    }

    if "json" in request_data and not files_spec:
        kwargs["json"] = request_data["json"]
    if "params" in request_data:
        kwargs["params"] = request_data["params"]

    opened_handles: list = []
    try:
        if files_spec:
            multipart, opened_handles = build_multipart_files(files_spec)
            kwargs["files"] = multipart

        full_url = config.base_url + url
        return requests.request(method, full_url, **kwargs)
    finally:
        for handle in opened_handles:
            handle.close()
