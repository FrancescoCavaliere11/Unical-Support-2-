package unical_support.unicalsupport2.service.interfaces;


public interface CategoryService {
    String listCategories();
    void createCategory(String name, String description);
    String createCategories(String pathFile);
    String deleteCategory(String name);
}
