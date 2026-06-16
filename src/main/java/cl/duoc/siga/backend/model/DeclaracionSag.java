package cl.duoc.siga.backend.model;

import cl.duoc.siga.backend.enums.ResultadoRevision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "declaracion_sag")
@Getter
@Setter
@NoArgsConstructor
public class DeclaracionSag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tramite_id", nullable = false, unique = true)
    private TramiteAduanero tramite;

    @Column(name = "transporta_productos", nullable = false)
    private boolean transportaProductos = false;

    @Column(name = "detalle_productos", length = 2000)
    private String detalleProductos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_sag_id")
    private Usuario funcionarioSag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ResultadoRevision resultado = ResultadoRevision.PENDIENTE;

    @Column(name = "fecha_revision")
    private OffsetDateTime fechaRevision;
}
