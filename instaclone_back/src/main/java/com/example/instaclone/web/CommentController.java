package com.example.instaclone.web;

import com.example.instaclone.dto.CommentDTO;
import com.example.instaclone.entity.Comment;
import com.example.instaclone.facade.CommentFacade;
import com.example.instaclone.payload.response.MessageResponse;
import com.example.instaclone.services.CommentService;
import com.example.instaclone.validations.ResponseErrorValidation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("api/comment")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentFacade commentFacade;

    @Autowired
    private ResponseErrorValidation responseErrorValidation;

    @PostMapping("/{postId}/create")
    public ResponseEntity<Object> createComment(@Valid @RequestBody CommentDTO commentDTO,
                                                @PathVariable("postId") String postId,
                                                BindingResult bindingResult,
                                                Principal principal) {
        ResponseEntity<Object> errors = responseErrorValidation.mapValidationService(bindingResult);
        if (!ObjectUtils.isEmpty(errors)) return errors;

        Comment comment = commentService.saveComment(Long.parseLong(postId), commentDTO, principal);
        CommentDTO createdComment = commentFacade.commentToCommentDTO(comment);
        return new ResponseEntity<>(createdComment, HttpStatus.OK);
    }

    @GetMapping("/{postId}/all")
    public ResponseEntity<List<CommentDTO>> getAllCommentsToPost(@PathVariable("postId") String postId) {
        List<CommentDTO> commentDTOList = commentService.getAllCommentsForPost(Long.parseLong(postId))
                .stream()
                .map(commentFacade::commentToCommentDTO)
                .toList();

        return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
    }

    @PostMapping("/{commentId}/delete")
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable("commentId") String commentId) {
        commentService.DeleteComment(Long.parseLong(commentId));
        return new ResponseEntity<>(new MessageResponse("Comment was deleted"), HttpStatus.OK);
    }
}
