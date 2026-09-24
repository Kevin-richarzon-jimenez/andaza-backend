package com.andanza.backend.admin.category;

import com.andanza.backend.catalog.Category;
import com.andanza.backend.catalog.CategoryRepository;
import com.andanza.backend.catalog.CategoryResponse;
import com.andanza.backend.catalog.ProductRepository;
import com.andanza.backend.exception.ConflictException;
import com.andanza.backend.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AdminCategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CategoryResponse create(AdminCategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("name", "Ya existe una categoría con ese nombre");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(request.description().trim());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("id", "La categoría indicada no existe"));
        long products = productRepository.countByCategoryId(id);
        if (products > 0) {
            throw new ConflictException("id", "No se puede eliminar la categoría: tiene " + products + " producto(s) asociado(s)");
        }
        categoryRepository.delete(category);
    }
}
