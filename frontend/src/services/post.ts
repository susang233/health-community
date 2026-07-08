// services/post.ts
import {api,adminApi} from '@/services/axios';
import type { AdminPostDetailVO, PostIndexVO, PostLikeVO, PostListType, PostPageVO, PostReviewDTO, PostStatus } from '@/types/post';
// 上传图片（接收 FormData）
export const uploadPostImages = (formData: FormData): Promise<string[]> => {
  return api.post("/user/post/upload-post-images", formData);
  
};


// 发帖（接收 JSON）
export const createPost = (data: { content: string; imageUrls: string[] }) => {
  return api.post("/user/post/create", data);
};
//管理员获取帖子列表（可选状态过滤）
export const getPosts = (page: number, status?: PostStatus) :Promise<PostPageVO>=> {
  return adminApi.get("/admin/post", {
    params: {
      page,
      status
    }
  });
};

export const getPostDetail = (postId: number): Promise<AdminPostDetailVO> => {
  return adminApi.get(`/admin/post/${postId}`);
};


export const reviewPost = (data:PostReviewDTO) :Promise<boolean>=> {
  return adminApi.post("/admin/post/review", data);
}

export const getPostList = ( page: number,type?:PostListType ) :Promise<PostIndexVO>=> {
  return api.get("/user/post/index", {
    params: { 
      page,
      type,
    },
  });
}

export const likePost = (postId: number): Promise<PostLikeVO> => {
  return api.post(`/user/post/${postId}/like`);
};
export const getUserPostList = ( page: number,userId?:number ) :Promise<PostIndexVO>=> {
  return api.get("/user/post", {
    params: { 
      page,
      userId,
    },
  });
}

import type { PostDetail, PostVO } from '@/types/post';
import type { CommentListVO } from '@/types/post';

// 获取帖子详情
export const getUserPostDetail = async (postId: number): Promise<PostDetail> => {

  const postVO = await api.get(`/user/post/${postId}`) as unknown as PostVO;

  return {
    id: postVO.id,
    content: postVO.content,
    status: postVO.status as PostStatus,
    rejectReason: postVO.rejectReason,
    likeCount: postVO.likeCount,
    isLike: postVO.isLike,
    isOwnPost: postVO.isOwnPost,
    commentCount: postVO.commentCount,
    postImageList: postVO.postImageList,
    createTime: postVO.createTime,
    updateTime: postVO.updateTime,
    author: {
      userId: postVO.userId,
      avatarUrl: postVO.avatarUrl,
      nickName: postVO.nickName,
      isFollow: postVO.isFollow,
      profileText: postVO.profileText,
    },
  };
};

export const getPostComments=(postId: number, page: number = 1): Promise<CommentListVO>=> {
   return   api.get(`/user/post/comment/${postId}`, { params: { page } });
 
}




export const addComment=(postId: number, content: string) => {
  return api.post('/user/post/comment/create', { postId, content });
}


export const deletePost=(postId: number) => {
  return api.delete(`/user/post/${postId}`);
}

export const updatePost = (data: {id:number; content: string; imageUrls: string[] }) => {
  return api.put("/user/post", data);
};