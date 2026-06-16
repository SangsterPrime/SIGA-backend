package cl.duoc.siga.backend.model;

import cl.duoc.siga.backend.enums.EstadoTramite;
import cl.duoc.siga.backend.enums.TipoTramite;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tramite_aduanero")
@Getter
@Setter
@NoArgsConstructor
public class TramiteAduanero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String folio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pasajero_id", nullable = false)
    private Usuario pasajero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_asignado_id")
    private Usuario funcionarioAsignado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tramite", nullable = false, length = 20)
    private TipoTramite tipoTramite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoTramite estado = EstadoTramite.BORRADOR;

    @Column(name = "url_comprobante", length = 500)
    private String urlComprobante;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;
}
