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
@Table(name = "revision_pdi")
@Getter
@Setter
@NoArgsConstructor
public class RevisionPdi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tramite_id", nullable = false, unique = true)
    private TramiteAduanero tramite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_pdi_id")
    private Usuario funcionarioPdi;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_revision", nullable = false, length = 15)
    private ResultadoRevision estadoRevision = ResultadoRevision.PENDIENTE;

    @Column(length = 2000)
    private String observaciones;

    @Column(name = "fecha_revision")
    private OffsetDateTime fechaRevision;
}
