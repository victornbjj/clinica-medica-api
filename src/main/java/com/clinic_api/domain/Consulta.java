package com.clinic_api.domain;

import com.clinic_api.enums.StatusConsulta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;



@Entity
@Table(name = "tb_consultas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Consulta {


     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private UUID id;

     @Column(nullable = false)
     private LocalDateTime dataHora;

     @Enumerated(EnumType.STRING)
     @Column(nullable = false)
     private StatusConsulta status;

     @ManyToOne
     @JoinColumn(name = "id_medico", nullable = false)
     private Medico medico;

     @ManyToOne
     @JoinColumn(name = "paciente_medico", nullable = false)
     private Paciente paciente;
}
