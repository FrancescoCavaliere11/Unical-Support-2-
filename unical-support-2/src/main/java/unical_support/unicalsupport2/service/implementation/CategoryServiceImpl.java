package unical_support.unicalsupport2.service.implementation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import unical_support.unicalsupport2.data.entities.Category;
import unical_support.unicalsupport2.data.repositories.CategoryRepository;
import unical_support.unicalsupport2.service.interfaces.CategoryService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public void createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        categoryRepository.save(category);
    }

    @Override
    public String createCategories(String pathFile) {
        try {
            // Creo un'istanza di ObjectMapper per leggere/scrivere da un file JSON
            ObjectMapper mapper = new ObjectMapper();

            // Leggo il contenuto del file JSON e lo converto in una lista di mappe
            List<Map<String, String>> categoriesFromJson = mapper.readValue(
                    new File(pathFile),
                    new TypeReference<>() {
                    }
            );

            log.info("Found {} categories.", categoriesFromJson.size());

            List<Category> categories = categoriesFromJson
                    .stream()
                    .map(category -> {
                        String name = category.get("name");
                        String description = category.get("description");

                        if (!validateCategoryData(name, description)) {
                            log.warn("Invalid or already existing category: name='{}', description='{}'. Skipping.", name, description);
                            return null;
                        }

                        Category c = new Category();
                        c.setName(name);
                        c.setDescription(description);

                        return c;
                    })
                    .filter(Objects::nonNull)
                    .toList();

            Integer categoryCreated = categoryRepository.saveAll(categories).size();

            return categoryCreated + " categories imported successfully.";
        } catch (IOException e) {
            return "Error reading JSON file: " + e.getMessage();
        }
    }

    @Override
    public String deleteCategory(String name) {
        Category category = categoryRepository.findByName(name)
                .orElse(null);

        if (category == null) {
            return "Category with name '" + name + "' not found.";
        } else {
            categoryRepository.delete(category);
            return "Category with name '" + name + "' deleted successfully.";
        }
    }

    @Override
    public String listCategories() {
        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) log.warn("No categories found.");
        else log.info("Listing all categories:");

        StringBuilder sb = new StringBuilder();
        for (Category category : categories) {
            sb.append(category.toString());
        }
        return sb.toString();
    }

    private boolean validateCategoryData(String name, String description) {
        if (categoryRepository.existsByNameIgnoreCase(name))
            return false;

        if (name == null || name.length() < 3 || name.length() > 50)
            return false;

        return description != null && !description.isBlank() && description.length() >= 10 && description.length() <= 500;
    }
}
