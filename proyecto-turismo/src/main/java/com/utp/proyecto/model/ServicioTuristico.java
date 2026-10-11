package com.utp.proyecto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.utp.proyecto.validation.ValidacionSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Entity
@Table(name = "servicios_turisticos")
public class ServicioTuristico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(groups = ValidacionSolicitud.class)
    @Column(nullable = false, length = 150)
    private String nombre;
    @NotBlank(groups = ValidacionSolicitud.class)
    @Column(nullable = false, length = 60)
    private String tipo;
    @NotBlank(groups = ValidacionSolicitud.class)
    @Column(nullable = false, length = 2000)
    private String descripcion;
    @NotBlank(groups = ValidacionSolicitud.class)
    @Column(nullable = false, length = 255)
    private String horarios;
    @NotNull(groups = ValidacionSolicitud.class)
    @PositiveOrZero(groups = ValidacionSolicitud.class)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    @NotBlank(groups = ValidacionSolicitud.class)
    @Column(length = 255)
    private String ruta;
    @NotBlank(groups = ValidacionSolicitud.class)
    @Column(nullable = false, length = 180)
    private String contacto;
    @NotNull(groups = ValidacionSolicitud.class)
    @Column(nullable = false)
    private Boolean aceptaReservas;
    @Column(nullable = false)
    private Integer atenciones;
    @Column(nullable = false)
    private Double calificacion;
    @Column(nullable = false)
    private Boolean activo;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id")
    private Usuario creador;

    public ServicioTuristico() {
    }

    public ServicioTuristico(Long id, String nombre, String tipo, String descripcion, String horarios,
                             BigDecimal precio, String ruta, String contacto, Boolean aceptaReservas,
                             Integer atenciones, Double calificacion, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.horarios = horarios;
        this.precio = precio;
        this.ruta = ruta;
        this.contacto = contacto;
        this.aceptaReservas = aceptaReservas;
        this.atenciones = atenciones;
        this.calificacion = calificacion;
        this.activo = activo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getHorarios() { return horarios; }
    public void setHorarios(String horarios) { this.horarios = horarios; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public Boolean getAceptaReservas() { return aceptaReservas; }
    public void setAceptaReservas(Boolean aceptaReservas) { this.aceptaReservas = aceptaReservas; }

    public Integer getAtenciones() { return atenciones; }
    public void setAtenciones(Integer atenciones) { this.atenciones = atenciones; }

    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }
}
