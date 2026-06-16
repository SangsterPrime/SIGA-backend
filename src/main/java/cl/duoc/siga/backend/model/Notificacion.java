package cl.duoc.siga.backend.model;

import cl.duoc.siga.backend.enums.CanalNotificacion;
import cl.duoc.siga.backend.enums.EstadoEnvio;
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

import java.time.OffsetDateTime;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tramite_id", nullable = false)
    private TramiteAduanero tramite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_destino_id", nullable = false)
    private Usuario usuarioDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CanalNotificacion canal;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio", nullable = false, length = 15)
    private EstadoEnvio estadoEnvio = EstadoEnvio.PENDIENTE;

    @Column(name = "fecha_envio")
    private OffsetDateTime fechaEnvio;
}
