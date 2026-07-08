import type { Gender } from "./gender";
import type { UserVO } from "./user";

export enum PostStatus {
  PENDING = "PENDING",
  APPROVED = "APPROVED",
  REJECTED = "REJECTED",
}
export interface Post {
  id: number;
  userId: number;
  content: string;
  status: PostStatus;
  rejectReason: string | null;
  likeCount: number;
  commentCount: number;
  createTime: string;
  updateTime: string;
}
export interface PostPageVO {
  content: {
    id: number;
    userId: number;
    content: string;
    status: PostStatus;
    rejectReason: string | null;
    likeCount: number;
    commentCount: number;
    createTime: string;
    updateTime: string;
  }[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      unsorted: boolean;
      sorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements?: number;
  totalPages?: number;
}
export interface AdminPostDetailVO {
  id: number;
  userId: number;
  content: string;
  status: PostStatus;
  rejectReason: string | null;
  createTime: string;
  imageUrls: string[];
}
export interface PostReviewDTO {
  postId: number;
  
  status: PostStatus; // 必须是 APPROVED 或 REJECTED
  rejectReason?: string; // 当 status == REJECTED 时必填
  userId?: number;
}

export interface PostIndexVO {
  page: number;
  hasNext: boolean;
  posts: PostVO[];
}
export interface PostVO {
  id: number;
  userId: number;
  gender: Gender;
  avatarUrl: string;
  nickName: string;
  profileText: string; // 这个字段已经包含了 "169cm｜90.0kg｜BMI 31.5｜上班族,宝妈"
  content: string;
  status: PostStatus;
  rejectReason: string;
  likeCount: number;
  commentCount: number;
  isFollow: boolean; // 当前登录用户是否已关注作者
  isLike: boolean; // 当前登录用户是否已点赞
  isOwnPost:boolean; // 是否是自己的帖子
  postImageList: {
    id: number;
    postId: number;
    imageUrl: string;
    sortIndex: number;
  }[];
  createTime: string;
  updateTime: string;
}
export interface PostDetail {
  id: number;
  content: string;
  status: PostStatus;
  rejectReason: string;
  likeCount: number;
  isLike: boolean | null;
  isOwnPost:boolean;
  commentCount: number;
  postImageList: {  id: number;
    postId: number;
    imageUrl: string;
    sortIndex: number;}[];
  createTime: string;
  updateTime: string;

  //  作者信息（聚合字段）
  author: {
    userId: number;
    avatarUrl?: string;
    nickName: string;
    isFollow: boolean | null;
    profileText?: string;
  };
}

export enum PostListType {
    RECOMMEND="RECOMMEND",   // 推荐
    FOLLOWING="FOLLOWING",   // 关注
}

export interface PostLikeVO{
    liked:boolean;
    postId:number;
    likeCount:number;
}

export interface CommentVO {
  id: number;
  content: string;
  createTime: string;
  userVO: UserVO;
}

export interface CommentListVO {
    commentVOList: CommentVO[];
    hasNext: boolean;
    page: number;
}