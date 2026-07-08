export interface UserVO {
  userId: number;
  avatarUrl?: string;
  nickName: string;
  isFollow: boolean;
  followersCount: number;
  followingCount: number;
  postCount: number;
}

export interface AdminPageVO {
  content: {
    userId: number;
    username: string;
    nickname: string;
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

export interface AdminInitVO {
  userId: number;

  username: string;
  nickname: string;

  rawPassword: string;
}
export interface AdminCreateDTO {
 
  username: string;
  password?: string;
}

export interface AdminPageQuery {
  name?: string;

  page: number;
  size: number;
}