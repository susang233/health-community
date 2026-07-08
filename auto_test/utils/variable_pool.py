"""
变量池（Variable Pool）

你可以把变量池想象成一个「字典 / 储物柜」：
  - 键（key）  ：变量名，例如 "token"、"user_id"
  - 值（value）：变量的实际内容，例如 "eyJhbGciOiJIUzI1NiJ9..."

在接口自动化里，变量池用来解决「数据关联」问题：
  1. 登录接口返回 token  →  extract 模块把 token 放进变量池
  2. 发帖接口需要 token  →  YAML 里写 Authorization: Bearer ${token}
  3. render 模块从变量池取出 token，替换掉占位符，再发请求

本文件只负责「存」和「取」，不负责替换（替换在 render.py 里做）。
"""
from __future__ import annotations

import os
from typing import Any


class VariablePool:
    """
    变量池类。

    Python 类（class）说明（给初学者）：
      - class 用来把一组相关的数据和函数打包在一起
      - def __init__ 是「构造函数」，创建对象时自动执行
      - self 代表「当前这个对象自己」，通过 self.xxx 访问自己的属性
    """

    def __init__(self) -> None:
        """
        创建一个空的变量池。

        self._vars 前面的下划线 _ 是一种约定，表示「内部使用，外部尽量不直接改」。
        外部应通过 set() / get() 来操作，这样以后加日志、校验更方便。
        """
        # dict（字典）是 Python 的键值对容器，类似 Java 的 Map
        self._vars: dict[str, Any] = {}

    def set(self, key: str, value: Any) -> None:
        """
        往变量池里放入（或覆盖）一个变量。

        参数:
            key  : 变量名，例如 "token"
            value: 变量值，可以是字符串、数字、布尔值等任意类型

        示例:
            pool.set("token", "abc123")
        """
        self._vars[key] = value

    def get(self, key: str, default: Any = None) -> Any:
        """
        从变量池读取变量。

        参数:
            key    : 要读取的变量名
            default: 如果找不到这个 key，返回 default（默认是 None）

        dict.get(key, default) 比 dict[key] 更安全：
          - dict[key] 在 key 不存在时会抛 KeyError 异常
          - dict.get(key, default) 找不到就返回 default，不报错
        """
        return self._vars.get(key, default)

    def update(self, mapping: dict[str, Any]) -> None:
        """
        批量更新变量池（一次写入多个变量）。

        dict.update(另一个字典) 会把另一个字典里的键值对合并进来；
        如果 key 已存在，新值会覆盖旧值。

        参数:
            mapping: 例如 {"token": "xxx", "user_id": 1001}

        示例（extract 模块将来会这样调用）:
            pool.update({"token": "abc", "user_id": 1})
        """
        self._vars.update(mapping)

    def clear(self) -> None:
        """
        清空变量池。

        每条用例（case）执行前，Runner 会 clear() 一次，
        避免上一条用例的 token 污染下一条用例。
        """
        self._vars.clear()

    def has(self, key: str) -> bool:
        """
        判断变量池里是否存在某个 key。

        `key in dict` 是 Python 判断「键是否在字典里」的写法。
        """
        return key in self._vars

    def as_dict(self) -> dict[str, Any]:
        """
        返回变量池的副本（拷贝），用于调试或打印日志。

        dict(self._vars) 会创建一个新字典，外面改它不会影响池子内部。
        """
        return dict(self._vars)

    def load_env(self, *keys: str) -> None:
        """
        从操作系统环境变量（包括 .env 加载进来的）批量写入变量池。

        为什么需要这个方法？
          YAML 用例里常写 ${USER_USERNAME}，这个名字和 .env 里的变量名一致。
          执行前把环境变量同步进变量池，render 就只需查一个地方。

        参数:
            keys: 要加载的变量名列表。
                  如果不传，则加载下面 ENV_KEYS 里列出的常用测试账号变量。

        os.getenv("NAME") 读取环境变量；不存在时返回 None。
        """
        # 默认要加载的环境变量名（与 .env、config/setting.py 保持一致）
        default_keys = (
            "USER_USERNAME",
            "USER_PASSWORD",
            "ADMIN_USERNAME",
            "ADMIN_PASSWORD",
            "SUPERADMIN_USERNAME",
            "SUPERADMIN_PASSWORD",
            "TEST_ENV",
        )
        # 如果调用时传了 keys，用传入的；否则用 default_keys
        # `keys if keys else default_keys` 是三元表达式的变体
        target_keys = keys if keys else default_keys

        for name in target_keys:
            value = os.getenv(name)
            # 只有环境变量「确实存在且不为空」才写入，避免用 None 覆盖已有值
            if value is not None and value != "":
                self.set(name, value)

    def resolve(self, key: str) -> Any:
        """
        按优先级解析变量值（给 render 模块调用）。

        查找顺序:
          1. 变量池自身（extract 写入的运行时变量，优先级最高）
          2. 操作系统环境变量（.env 里的 USER_USERNAME 等）

        参数:
            key: 变量名，不含 ${}，例如 "token" 或 "USER_USERNAME"

        返回:
            解析到的值

        抛出:
            KeyError: 两个地方都找不到时，抛出带提示信息的异常
        """
        # 第一步：先查变量池（运行时 extract 的数据优先）
        if self.has(key):
            return self.get(key)

        # 第二步：再查环境变量
        env_value = os.getenv(key)
        if env_value is not None and env_value != "":
            return env_value

        # 都找不到，抛出清晰的错误，方便排查是忘了 extract 还是忘了配 .env
        raise KeyError(
            f"变量 '{key}' 未定义：变量池中不存在，环境变量里也没有。"
            f"请检查：1) 上一步是否 extract 了该变量；2) .env 是否配置了 {key}"
        )
