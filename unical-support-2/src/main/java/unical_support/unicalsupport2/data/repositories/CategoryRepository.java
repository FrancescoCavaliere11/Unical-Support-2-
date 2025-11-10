package unical_support.unicalsupport2.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unical_support.unicalsupport2.data.entities.Category;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCase(String name);
}
