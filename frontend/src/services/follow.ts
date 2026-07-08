import {api} from '@/services/axios';
import type { FolloweePageVO, FollowerPageVO } from '@/types/follow';


export const getFollower = ( page: number ) :Promise<FollowerPageVO>=> {
  return api.get("/user/follow/followers", {
    params: { 
      page,
     
    },
  });
}
export const getFollowee = ( page: number ) :Promise<FolloweePageVO>=> {
  return api.get("/user/follow/followees", {
    params: { 
      page,
     
    },
  });
}