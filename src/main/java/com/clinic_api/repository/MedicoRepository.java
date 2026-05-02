package com.clinic_api.repository;

import com.clinic_api.domain.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MedicoRepository extends JpaRepository<Medico,UUID> {
}
