package com.andanza.backend.comment;

import com.andanza.backend.catalog.PageResponse;
import com.andanza.backend.catalog.Product;
import com.andanza.backend.catalog.ProductRepository;
import com.andanza.backend.common.PageParams;
import com.andanza.backend.exception.NotFoundException;
import com.andanza.backend.user.User;
import com.andanza.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment commentWith(CommentStatus status) {
        User user = new User();
        user.setFirstName("Ana");
        user.setLastName("Lopez");
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Runner Air");
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setProduct(product);
        comment.setRating(5);
        comment.setText("Muy cómodos");
        comment.setStatus(status);
        return comment;
    }

    @Test
    void aNewCommentIsPublishedImmediately() {
        UUID userId = UUID.randomUUID();
        Comment template = commentWith(CommentStatus.PUBLISHED);
        when(productRepository.findById(template.getProduct().getId())).thenReturn(Optional.of(template.getProduct()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(template.getUser()));

        CommentResponse response = commentService.create(userId,
                new CommentRequest(template.getProduct().getId(), 5, "  Muy cómodos  "));

        assertThat(response.status()).isEqualTo(CommentStatus.PUBLISHED);
        assertThat(response.text()).isEqualTo("Muy cómodos");
        assertThat(response.authorName()).isEqualTo("Ana L.");
    }

    @Test
    void onlyPublishedCommentsAreListedForAProduct() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);
        when(commentRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, CommentStatus.PUBLISHED))
                .thenReturn(List.of(commentWith(CommentStatus.PUBLISHED)));

        assertThat(commentService.listPublished(productId)).hasSize(1);
    }

    @Test
    void listingCommentsOfAMissingProductIsNotFound() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThatThrownBy(() -> commentService.listPublished(productId)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void theAdminListShowsEveryStatusWhenNoFilterIsGiven() {
        when(commentRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(commentWith(CommentStatus.PUBLISHED), commentWith(CommentStatus.HIDDEN))));

        PageResponse<CommentResponse> page = commentService.listAll(null, new PageParams(null, null));

        assertThat(page.content()).extracting(CommentResponse::status)
                .containsExactly(CommentStatus.PUBLISHED, CommentStatus.HIDDEN);
        assertThat(page.totalElements()).isEqualTo(2);
    }

    @Test
    void theAdminListCanFilterByStatus() {
        when(commentRepository.findByStatus(eq(CommentStatus.HIDDEN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(commentWith(CommentStatus.HIDDEN))));

        PageResponse<CommentResponse> page = commentService.listAll(CommentStatus.HIDDEN, new PageParams(0, 10));

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).status()).isEqualTo(CommentStatus.HIDDEN);
    }

    @Test
    void anAdminCanHideAndShowAgainAComment() {
        UUID commentId = UUID.randomUUID();
        Comment comment = commentWith(CommentStatus.PUBLISHED);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThat(commentService.setVisibility(commentId, false).status()).isEqualTo(CommentStatus.HIDDEN);
        assertThat(commentService.setVisibility(commentId, true).status()).isEqualTo(CommentStatus.PUBLISHED);
    }

    @Test
    void changingTheVisibilityOfAMissingCommentIsNotFound() {
        UUID commentId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.setVisibility(commentId, false)).isInstanceOf(NotFoundException.class);
    }
}
