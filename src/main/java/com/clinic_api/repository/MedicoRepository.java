package com.clinic_api.repository;

import com.clinic_api.domain.Medico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MedicoRepository extends JpaRepository<Medico, UUID> {

    Optional<Medico> findByIdUserId(UUID idUser);

    Optional<Medico> findByCrm(String crm);

    Page<Medico> findByEspecialidadeContainingIgnoreCase(String especialidade, Pageable pageable);
}
