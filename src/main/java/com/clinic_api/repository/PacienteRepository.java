package com.clinic_api.repository;


import com.clinic_api.domain.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente,UUID> {
}
