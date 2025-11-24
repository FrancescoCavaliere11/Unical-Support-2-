package unical_support.unicalsupport2.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import unical_support.unicalsupport2.data.entities.EmailToClassify;

import java.util.List;

public interface EmailToClassifyRepository extends JpaRepository<EmailToClassify, String> {
    List<EmailToClassify> findAllByIsClassified(boolean isClassified);
}
