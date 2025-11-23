package unical_support.unicalsupport2.data.repositories;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import unical_support.unicalsupport2.data.entities.Email;

@Repository
public interface EmailRepository extends JpaRepository<Email, String>{

    List<Email> findByClassified(boolean classified);

    List<Email> findBySubject(String subject);
} 