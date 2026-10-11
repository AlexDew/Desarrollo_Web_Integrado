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

@Entity
@Table(name = "lugares_turisticos")
public class LugarTuristico {
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
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    @NotBlank(groups = ValidacionSolicitud.class)
    @Column(length = 255)
    private String direccion;
    @NotBlank(groups = ValidacionSolicitud.class)
    @Column(nullable = false, length = 255)
    private String horarios;
    @NotBlank(groups = ValidacionSolicitud.class)
    @Column(nullable = false, length = 180)
    private String contacto;
    @Column(nullable = false)
    private Integer visitantes;
    @Column(nullable = false)
    private Double calificacion;
    @Column(nullable = false)
    private Boolean activo;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id")
    private Usuario propietario;

    public LugarTuristico() {
    }

    public LugarTuristico(Long id, String nombre, String tipo, String descripcion,
                          String direccion, String horarios, String contacto,
                          Integer visitantes, Double calificacion, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.horarios = horarios;
        this.contacto = contacto;
        this.visitantes = visitantes;
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

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getHorarios() { return horarios; }
    public void setHorarios(String horarios) { this.horarios = horarios; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public Integer getVisitantes() { return visitantes; }
    public void setVisitantes(Integer visitantes) { this.visitantes = visitantes; }

    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Usuario getPropietario() { return propietario; }
    public void setPropietario(Usuario propietario) { this.propietario = propietario; }
}
