package cl.duoc.siga.backend.model;

import cl.duoc.siga.backend.enums.TipoFormulario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehiculo")
@Getter
@Setter
@NoArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tramite_id", nullable = false, unique = true)
    private TramiteAduanero tramite;

    @Column(nullable = false, length = 10)
    private String patente;

    @Column(length = 60)
    private String marca;

    @Column(length = 60)
    private String modelo;

    private Integer anio;

    @Column(name = "pais_registro", length = 40)
    private String paisRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_formulario", nullable = false, length = 20)
    private TipoFormulario tipoFormulario;
}
