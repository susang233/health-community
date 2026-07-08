"""
兼容入口：保留旧文件名，统一转发到按模块拆分后的测试。

说明：
1. 现在推荐直接运行 test_user_api.py / test_food_api.py
2. 保留本文件是为了避免你之前的命令、文档、历史习惯全部失效
3. 本文件不再放真实测试逻辑，只做兼容说明
"""

import pytest


pytestmark = pytest.mark.skip(reason="已按模块拆分，请使用 test_user_api.py / test_food_api.py")
