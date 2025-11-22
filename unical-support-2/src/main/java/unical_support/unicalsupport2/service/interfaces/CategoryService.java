package unical_support.unicalsupport2.service.interfaces;


import unical_support.unicalsupport2.data.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll();

    String listCategories();
    void createCategory(String name, String description);
    String createCategories(String pathFile);
    String deleteCategory(String name);
}
