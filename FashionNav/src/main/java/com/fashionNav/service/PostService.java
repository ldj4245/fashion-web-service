package com.fashionNav.service;

import com.fashionNav.model.entity.Comment;
import com.fashionNav.model.entity.File;
import com.fashionNav.model.entity.Post;
import com.fashionNav.model.entity.User;
import com.fashionNav.repository.CommentMapper;
import com.fashionNav.repository.FileMapper;
import com.fashionNav.repository.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final FileMapper fileMapper;

    private final Path rootLocation = Paths.get("upload-dir");

    public List<Post> getPostsByBoardType(String boardType) {
        List<Post> posts = postMapper.findPostsByBoardType(boardType);
        posts.forEach(post -> {
            User user = postMapper.findUserById(post.getUserId());
            post.setUserName(user.getName());
        });
        return posts;
    }

    public Post getPostById(int postId) {
        Post post = postMapper.findPostById(postId);
        User user = postMapper.findUserById(post.getUserId());
        post.setUserName(user.getName());
        return post;
    }

    public void createPost(Post post, MultipartFile file, Authentication authentication) {
        int userId = ((User) authentication.getPrincipal()).getUserId();  // 현재 로그인된 사용자 ID 설정
        post.setUserId(userId);
        postMapper.insertPost(post);
        if (file != null) {
            saveFile(post.getPostId(), file);
        }
    }

    public void updatePost(Post post, Authentication authentication) {
        int userId = ((User) authentication.getPrincipal()).getUserId();
        // 현재 사용자만 게시물을 수정할 수 있도록 제한
        if (postMapper.findPostById(post.getPostId()).getUserId() != userId) {
            throw new IllegalArgumentException("You do not have permission to edit this post");
        }
        postMapper.updatePost(post);
    }

    public void deletePost(int postId, Authentication authentication) {
        int userId = ((User) authentication.getPrincipal()).getUserId();
        // 현재 사용자만 게시물을 삭제할 수 있도록 제한
        if (postMapper.findPostById(postId).getUserId() != userId) {
            throw new IllegalArgumentException("You do not have permission to delete this post");
        }
        // 게시물 삭제 전에 관련된 댓글을 먼저 삭제
        commentMapper.deleteCommentsByPostId(postId);
        postMapper.deletePost(postId);
    }

    public List<Comment> getCommentsByPostId(int postId) {
        List<Comment> comments = commentMapper.findCommentsByPostId(postId);
        comments.forEach(comment -> {
            User user = commentMapper.findUserById(comment.getUserId());
            comment.setUserName(user.getName());
        });
        return comments;
    }

    public Comment getCommentById(int commentId) {
        Comment comment = commentMapper.findCommentById(commentId);
        User user = commentMapper.findUserById(comment.getUserId());
        comment.setUserName(user.getName());
        return comment;
    }

    public void createComment(Comment comment, Authentication authentication) {
        int userId = ((User) authentication.getPrincipal()).getUserId();  // 현재 로그인된 사용자 ID 설정
        comment.setUserId(userId);
        commentMapper.insertComment(comment);
    }

    public void updateComment(Comment comment, Authentication authentication) {
        int userId = ((User) authentication.getPrincipal()).getUserId();
        // 현재 사용자만 댓글을 수정할 수 있도록 제한
        if (commentMapper.findCommentById(comment.getCommentId()).getUserId() != userId) {
            throw new IllegalArgumentException("You do not have permission to edit this comment");
        }
        commentMapper.updateComment(comment);
    }

    public void deleteComment(int commentId, Authentication authentication) {
        int userId = ((User) authentication.getPrincipal()).getUserId();
        Comment comment = commentMapper.findCommentById(commentId);
        // 현재 사용자만 댓글을 삭제할 수 있도록 제한
        if (comment.getUserId() != userId) {
            throw new IllegalArgumentException("You do not have permission to delete this comment");
        }
        commentMapper.deleteComment(commentId);
    }

    public List<File> getFilesByPostId(int postId) {
        return fileMapper.findFilesByPostId(postId);
    }

    public void createFile(int postId, MultipartFile file) {
        saveFile(postId, file);
    }

    public void deleteFile(int fileId) {
        fileMapper.deleteFile(fileId);
    }

    private void saveFile(int postId, MultipartFile file) {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
            String filename = file.getOriginalFilename();
            Path destinationFile = rootLocation.resolve(
                            Paths.get(filename))
                    .normalize().toAbsolutePath();
            file.transferTo(destinationFile);

            File newFile = new File();
            newFile.setPostId(postId);
            newFile.setFileName(filename);
            newFile.setFilePath(destinationFile.toString());
            fileMapper.insertFile(newFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}