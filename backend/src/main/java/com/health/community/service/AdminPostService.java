package com.health.community.service;

import com.health.community.common.enumeration.PostStatus;
import com.health.community.common.exception.BusinessException;
import com.health.community.vo.AdminPostDetailVO;
import com.health.community.dto.PostReviewDTO;
import com.health.community.entity.Post;
import com.health.community.repository.PostImageRepository;
import com.health.community.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.health.community.common.constant.SizeConstant.PAGE_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor // 自动生成构造器
@Transactional
public class AdminPostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    // 获取待审核帖子列表（分页）
    public Page<Post> getPendingPosts(int page) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE, Sort.by("createTime").ascending());
        return postRepository.findByStatus(PostStatus.PENDING, pageable);
    }

    // 获取所有帖子列表（分页）
    public Page<Post> getPosts(int page) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), PAGE_SIZE, Sort.by("createTime").ascending());
        return postRepository.findAll(pageable);
    }

    //  查看帖子详情
    public AdminPostDetailVO getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        List<String> imageUrls = postImageRepository.findImageUrlsByPostIdOrderBySortIndex(postId);

       return AdminPostDetailVO.builder().id(post.getId())
               .userId(post.getUserId())
               .content(post.getContent())
               .status(post.getStatus())
               .rejectReason(post.getRejectReason())
               .createTime(post.getCreateTime())
               .imageUrls(imageUrls).build();

    }

    //  审核帖子
    @Transactional
    public void reviewPost(PostReviewDTO reviewDTO) {
        Post post = postRepository.findById(reviewDTO.getPostId())
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        // 只能审核待审核的帖子
        if (post.getStatus() != PostStatus.PENDING) {
            throw new BusinessException("该帖子无需审核");
        }

        PostStatus newStatus = reviewDTO.getStatus();
        if (newStatus == PostStatus.APPROVED) {
            post.setRejectReason(null); // 清空拒绝原因
        } else if (newStatus == PostStatus.REJECTED) {
            if (StringUtils.isBlank(reviewDTO.getRejectReason())) {
                throw new BusinessException("拒绝时必须填写原因");
            }
            post.setRejectReason(reviewDTO.getRejectReason());
        } else {
            throw new BusinessException("无效的审核状态");
        }

        post.setStatus(newStatus);
        postRepository.save(post);
    }
}