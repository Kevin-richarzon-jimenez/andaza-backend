package com.andanza.backend.storage;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupabaseStorageTest {

    @Test
    void withoutConfigurationItIsNotConfiguredAndUploadsAreRefused() {
        SupabaseStorage storage = new SupabaseStorage("", "", "product-images");

        assertThat(storage.isConfigured()).isFalse();
        assertThatThrownBy(() -> storage.upload("a.webp", new byte[] {1}, "image/webp"))
                .isInstanceOfSatisfying(StorageException.class,
                        e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void needsBothTheUrlAndTheKey() {
        assertThat(new SupabaseStorage("https://x.supabase.co", "", "b").isConfigured()).isFalse();
        assertThat(new SupabaseStorage("", "llave", "b").isConfigured()).isFalse();
        assertThat(new SupabaseStorage("https://x.supabase.co", "llave", "b").isConfigured()).isTrue();
    }

    @Test
    void buildsThePublicUrlIgnoringTrailingSlashesAndPathOfUndoesIt() {
        SupabaseStorage storage = new SupabaseStorage(" https://x.supabase.co/ ", "llave", "product-images");

        String url = storage.publicUrl("products/abc/1.webp");

        assertThat(url).isEqualTo("https://x.supabase.co/storage/v1/object/public/product-images/products/abc/1.webp");
        assertThat(storage.pathOf(url)).isEqualTo("products/abc/1.webp");
    }

    @Test
    void pathOfIgnoresAddressesThatAreNotOfThisBucket() {
        SupabaseStorage storage = new SupabaseStorage("https://x.supabase.co", "llave", "product-images");

        assertThat(storage.pathOf("https://otro.com/imagen.webp")).isNull();
        assertThat(storage.pathOf(null)).isNull();
    }
}
