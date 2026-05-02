package com.clinic_api.service;

import com.clinic_api.domain.Consulta;
import com.clinic_api.enums.Role;


import java.util.List;
import java.util.UUID;

public interface ConsultaService {

    List<Consulta> findByAll();

    List<Consulta> findByRole(Role role);

    Consulta findById(UUID id);

    void save(Consulta consulta);

    void update(Consulta consulta);

    void delet(UUID id);


}
