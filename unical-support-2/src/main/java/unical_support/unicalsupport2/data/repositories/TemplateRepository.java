package unical_support.unicalsupport2.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import unical_support.unicalsupport2.data.entities.Template;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, String> {

    List<Template> findByCategoryNameIgnoreCase(String categoryName);
    Boolean existsByNameIgnoreCase(String name);
    Boolean existsByNameIgnoreCaseAndIdNot(String name, String id);
    Optional<Template> findByNameIgnoreCase(String name);
}
