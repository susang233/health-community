// src/components/community/CreatePostForm.tsx
import { useEffect, useState } from "react";
import { Input, Upload, Button, message, Space } from "antd";
import { PictureOutlined } from "@ant-design/icons";
import type { RcFile, UploadFile } from "antd/es/upload/interface";
import { createPost, updatePost, uploadPostImages } from "@/services/post";

 interface CreatePostFormProps {
  onSuccess: () => void;
  onCancel: () => void;
  // 新增：可选的初始数据（用于编辑）
  initialData?: {
    id: number;          // 帖子 ID（编辑必需）
    content: string;
    imageUrls: string[]; // 已上传的图片 URL 列表
  };
}

export default function CreatePostForm({ onSuccess, onCancel,initialData  }: CreatePostFormProps) {
  const [content, setContent] = useState(initialData?.content || "");


  const [fileList, setFileList] = useState<UploadFile[]>(() => {
    if (initialData?.imageUrls) {
      // 将已有的图片 URL 转为 UploadFile 格式（只读，无 originFileObj）
      return initialData.imageUrls.map((url, index) => ({
        uid: `-1-${index}`, // 负数 uid 表示非本地文件
        name: `image-${index}.jpg`,
        status: 'done',
        url,
        // 注意：没有 originFileObj，所以不能重新上传这些图
      }));
    }
    return [];
  });

  const isEditMode = !!initialData;
  const postId = initialData?.id;
const [uploading, setUploading] = useState(false);

useEffect(() => {
  return () => {
    fileList.forEach(file => {
      if (file.url?.startsWith('blob:')) {
        URL.revokeObjectURL(file.url);
      }
    });
  };
}, [fileList]);

  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setContent(e.target.value);
  };

  const beforeUpload = (file: RcFile) => {
    // 只允许图片
    const isImage = file.type.startsWith("image/");
    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      message.error("图片不能超过 10MB!");
      return false;
    }
    if (!isImage) {
      message.error("只能上传图片！");
      return false;
    }
   
    return true; // 返回 true 才会加入 fileList
  };

 const handleChange = ({ fileList: newFileList }: { fileList: UploadFile[] }) => {
  // 过滤掉已移除的，并限制最多9张
  const filtered = newFileList
    .filter(file => file.status !== 'removed')
    .map(file => {
      // 为新文件生成 url（仅当是本地文件且无 url 时）
      if (file.originFileObj && !file.url) {
        return {
          ...file,
          url: URL.createObjectURL(file.originFileObj),
        };
      }
      return file;
    })
    .slice(0, 9);

  setFileList(filtered);
};
 const handleSubmit = async () => {
  if (!content.trim()) {
    message.error("请输入帖子内容");
    return;
  }

  try {
    setUploading(true);
    let imageUrls: string[] = [];

    // 1. 处理图片上传：只上传新增的本地文件
    const localFiles = fileList.filter(file => file.originFileObj); // 只有用户新选的才有 originFileObj
    if (localFiles.length > 0) {
      const formData = new FormData();
      localFiles.forEach(file => {
        formData.append("files", file.originFileObj!);
      });
      const uploadedUrls = await uploadPostImages(formData);
      imageUrls = uploadedUrls;
    }

    // 2. 合并：回显的图片 + 新上传的图片
    const existingUrls = fileList
      .filter(file => !file.originFileObj && file.url) // 回显的图（无 originFileObj）
      .map(file => file.url!);

    const finalImageUrls = [...existingUrls, ...imageUrls];

    // 3. 提交
    if (isEditMode && postId) {
      await updatePost({
        id: postId,
        content: content.trim(),
        imageUrls: finalImageUrls,
      });
      message.success("帖子更新成功！");
    } else {
      await createPost({
        content: content.trim(),
        imageUrls: finalImageUrls,
      });
      message.success("发帖成功，待审核后将在首页可见");
    }

    onSuccess();
  } catch (err) {
    console.error("操作失败:", err);
    message.error(isEditMode ? "更新失败，请重试" : "发帖失败，请重试");
  } finally {
    setUploading(false);
  }
};

  return (
    <div style={{ padding: "8px 0" }}>
      <Input.TextArea
        value={content}
        onChange={handleContentChange}
        placeholder="分享你的健康心得..."
        autoSize={{ minRows: 3, maxRows: 6 }}
        style={{ marginBottom: 16 }}
      />

      {/* 图片上传 */}
      <Upload
        listType="picture-card"
        fileList={fileList}
        beforeUpload={beforeUpload}
        onChange={handleChange}
        multiple
        showUploadList={{
          showPreviewIcon: true,
          showRemoveIcon: true,
        }}
      >
        {fileList.length < 9 && (
          <div>
            <PictureOutlined />
            <div style={{ marginTop: 8 }}>上传图片</div>
          </div>
        )}
      </Upload>

      <div style={{ textAlign: "right", marginTop: 16 }}>
        <Space>
          <Button onClick={onCancel}>取消</Button>
          <Button type="primary" onClick={handleSubmit} loading={uploading}>
            发布
          </Button>
        </Space>
      </div>
    </div>
  );
}