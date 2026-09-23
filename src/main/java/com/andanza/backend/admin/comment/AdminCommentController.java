package com.andanza.backend.admin.comment;

import com.andanza.backend.comment.CommentService;
import com.andanza.backend.comment.ModerateCommentRequest;
import com.andanza.backend.common.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/comments")
@Tag(name = "Admin - Comments")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> moderate(@PathVariable String id,
                                                       @Valid @RequestBody ModerateCommentRequest request) {
        String result = commentService.moderate(id, request.approve());
        return ResponseEntity.ok(new MessageResponse(result));
    }
}
