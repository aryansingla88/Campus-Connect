package com.campus.Campus_Connect.features.post.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.post.dto.request.CreatePostRequest;
import com.campus.Campus_Connect.features.post.dto.response.PostResponse;
import com.campus.Campus_Connect.features.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(

            @RequestPart("title")
            String title,

            @RequestPart("body")
            String body,

            @RequestPart("postType")
            String postType,

            @RequestPart("tags")
            List<Integer> tags,

            @RequestPart(value = "image", required = false)
            MultipartFile image
    ) {

        CreatePostRequest request = CreatePostRequest.builder()
                .title(title)
                .body(body)
                .postType(postType)
                .tags(tags)
                .image(image)
                .build();

        PostResponse response = postService.createPost(request);

        return ApiResponse.success(
                response,
                "Post created successfully."
        );
    }
}