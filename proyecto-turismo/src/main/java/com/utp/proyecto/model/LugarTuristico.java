package com.utp.proyecto.model;

public class LugarTuristico {
    private Long id;
    private String nombre;
    private String tipo;
    private String descripcion;
    private String direccion;
    private String horarios;
    private String contacto;
    private Integer visitantes;
    private Double calificacion;
    private Boolean activo;

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
}
