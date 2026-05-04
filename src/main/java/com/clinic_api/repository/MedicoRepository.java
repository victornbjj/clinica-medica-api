package com.clinic_api.repository;

import com.clinic_api.domain.Medico;
import com.clinic_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MedicoRepository extends JpaRepository<Medico,UUID> {
    Optional<Medico> findByIdUser(UUID idUser);

    Optional<Medico> findByCrm(String crm);
}
