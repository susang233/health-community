package com.health.community.service;

import com.health.community.common.context.UserContext;
import com.health.community.dto.CommentCreateDTO;
import com.health.community.entity.Comment;

import com.health.community.entity.User;
import com.health.community.repository.CommentRepository;
import com.health.community.vo.CommentListVO;
import com.health.community.vo.CommentVO;

import com.health.community.vo.UserVO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.health.community.common.constant.SizeConstant.PAGE_SIZE;

@Service

@RequiredArgsConstructor
public class CommentService {


    private final CommentRepository commentRepo;
    private final UserService userService;
    private final PostService postService; // 用于更新帖子的 likeCount
    @Transactional
    public CommentVO createComment(CommentCreateDTO commentCreateDTO) {
        Integer userId = UserContext.getCurrentUserId();
        Long postId = commentCreateDTO.getPostId();
        String content = commentCreateDTO.getContent();
        if (!postService.existsById(postId)) {
            throw new IllegalArgumentException("帖子不存在");
        }



        //  校验内容
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (content.length() > 200) {
            throw new IllegalArgumentException("评论不能超过200字");
        }



        //  保存评论
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .build();
        Comment saved = commentRepo.save(comment);

        //  更新帖子评论数
        postService.incrementCommentCount(postId);

        return CommentVO.builder().id(saved.getId())
                .content(saved.getContent())
                .userVO(userService.findUserVOByUserId(saved.getUserId()))
                .createTime(saved.getCreateTime()).build();
    }

    public CommentListVO getCommentVOList(Long postId, int page){
        if (!postService.existsById(postId)) {
            throw new IllegalArgumentException("帖子不存在");
        }


        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Comment> commentPage =commentRepo.findByPostId(postId,pageable);

        List<Integer> userIds = commentPage.getContent().stream().map(Comment::getUserId).distinct().toList();


        Map<Integer, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.findByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        List<CommentVO> list = commentPage.getContent().stream()
                .map(comment -> convertToCommentVO(comment, userMap))
                .toList();

        return CommentListVO.builder()
                .commentVOList(list)
                .page(page)
                .hasNext(commentPage.hasNext()).build();
    }

    private CommentVO convertToCommentVO(Comment comment, Map<Integer, User> userMap) {
        Integer userId = comment.getUserId();
        User user = userMap.get(userId);

        // 安全构建 UserVO：即使 user 为 null
        UserVO userVO;
        if (user != null) {
            userVO = UserVO.builder()
                    .userId(userId)
                    .avatarUrl(user.getAvatarUrl())
                    .nickName(user.getNickName())
                    .followersCount(user.getFollowersCount())
                    .followingCount(user.getFollowingCount())
                    .build();
        } else {
            // 用户已注销：显示默认头像和昵称
            userVO = UserVO.builder()
                    .userId(userId)
                    .avatarUrl("https://example.com/default-avatar.png") // 默认头像
                    .nickName("[已注销]") // 或 "[用户不存在]"
                    .followersCount(0)
                    .followingCount(0)
                    .build();
        }

        return CommentVO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userVO(userVO)
                .createTime(comment.getCreateTime())
                .build();
    }
}