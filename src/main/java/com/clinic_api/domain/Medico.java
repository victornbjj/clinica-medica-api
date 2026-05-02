package com.clinic_api.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tb_medicos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true,nullable = false)
    private String crm;

    @OneToOne
    @JoinColumn(name = "id_user", unique = true,nullable = false)
    private User idUser;

    @Column(nullable = false)
    private String especialidade;

}
