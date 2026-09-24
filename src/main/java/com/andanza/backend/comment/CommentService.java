package com.andanza.backend.comment;

import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductRepository;
import com.andanza.backend.exception.BusinessException;
import com.andanza.backend.exception.NotFoundException;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Este service lo usan dos controllers: CommentController (crear y consultar, lado cliente)
// y AdminCommentController (ocultar o volver a mostrar, lado admin). Todo comentario se publica al
// crearse; el administrador puede ocultarlo después y solo los publicados se ven en el producto.
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, ProductRepository productRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentResponse create(UUID userId, CommentRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException("productId", "El producto indicado no existe"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user", "El usuario no existe"));
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setProduct(product);
        comment.setRating(request.rating());
        comment.setText(request.text().trim());
        commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listPublished(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("productId", "El producto indicado no existe");
        }
        return commentRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, CommentStatus.PUBLISHED)
                .stream().map(CommentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listMine(UUID userId) {
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(CommentResponse::from).toList();
    }

    @Transactional
    public CommentResponse setVisibility(UUID commentId, boolean visible) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("id", "El comentario indicado no existe"));
        comment.setStatus(visible ? CommentStatus.PUBLISHED : CommentStatus.HIDDEN);
        return CommentResponse.from(comment);
    }
}
