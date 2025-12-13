package unical_support.unicalsupport2.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import unical_support.unicalsupport2.data.entities.Email;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<Email, String>{
    Optional<Email> findByAnswers_Id(String id);
} 