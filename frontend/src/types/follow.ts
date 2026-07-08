import type { UserVO } from "./user";

export interface FolloweePageVO {
  page: number;
  hasNext: boolean;
  followees: UserVO[];
}
export interface FollowerPageVO {
  page: number;
  hasNext: boolean;
  followers: UserVO[];
}