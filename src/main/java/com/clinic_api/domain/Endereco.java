package com.clinic_api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;



@Entity
@Table(name = "tb_enderecos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Endereco {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonProperty("logradouro")
    @Column(nullable = false)
    private String rua;

    @Column(nullable = true)
    private String numero;

    @JsonProperty("localidade")
    @Column(nullable = false)
    private String cidade;

    @JsonProperty("uf")
    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String cep;

    @Column(nullable = true)
    private String complemento;

    @OneToMany(mappedBy = "endereco")
    private List<User> users;
}



