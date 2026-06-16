package cl.duoc.siga.backend.model;

import cl.duoc.siga.backend.enums.EstadoValidacion;
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

import java.time.OffsetDateTime;

@Entity
@Table(name = "validacion_aduana_arg")
@Getter
@Setter
@NoArgsConstructor
public class ValidacionAduanaArg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tramite_id", nullable = false, unique = true)
    private TramiteAduanero tramite;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 15)
    private EstadoValidacion estadoValidacion = EstadoValidacion.PENDIENTE;

    @Column(name = "referencia_externa", length = 100)
    private String referenciaExterna;

    @Column(name = "respuesta_externa", length = 4000)
    private String respuestaExterna;

    @Column(nullable = false)
    private int intentos = 0;

    @Column(name = "fecha_validacion")
    private OffsetDateTime fechaValidacion;
}
