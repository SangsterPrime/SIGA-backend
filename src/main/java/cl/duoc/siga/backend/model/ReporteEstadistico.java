package cl.duoc.siga.backend.model;

import cl.duoc.siga.backend.enums.FormatoReporte;
import cl.duoc.siga.backend.enums.TipoReporte;
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

import java.time.OffsetDateTime;

@Entity
@Table(name = "reporte_estadistico")
@Getter
@Setter
@NoArgsConstructor
public class ReporteEstadistico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generado_por_id", nullable = false)
    private Usuario generadoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reporte", nullable = false, length = 15)
    private TipoReporte tipoReporte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FormatoReporte formato;

    @Column(length = 4000)
    private String parametros;

    @Column(name = "url_archivo", length = 500)
    private String urlArchivo;

    @CreationTimestamp
    @Column(name = "fecha_generacion", nullable = false, updatable = false)
    private OffsetDateTime fechaGeneracion;
}
