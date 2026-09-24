package com.andanza.backend.admin.comment;

import com.andanza.backend.comment.CommentResponse;
import com.andanza.backend.comment.CommentService;
import com.andanza.backend.comment.CommentVisibilityRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/comments")
@Tag(name = "Admin - Comments")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> setVisibility(@PathVariable UUID id,
                                                         @Valid @RequestBody CommentVisibilityRequest request) {
        return ResponseEntity.ok(commentService.setVisibility(id, request.visible()));
    }
}
