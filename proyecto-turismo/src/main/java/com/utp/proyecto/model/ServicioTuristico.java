package com.utp.proyecto.model;

public class ServicioTuristico {
    private Long id;
    private String nombre;
    private String tipo;
    private String descripcion;
    private String horarios;
    private Double precio;
    private String ruta;
    private String contacto;
    private Boolean aceptaReservas;
    private Integer atenciones;
    private Double calificacion;
    private Boolean activo;

    public ServicioTuristico() {
    }

    public ServicioTuristico(Long id, String nombre, String tipo, String descripcion,
                             String horarios, Double precio, String ruta, String contacto,
                             Boolean aceptaReservas, Integer atenciones, Double calificacion,
                             Boolean activo) {
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
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
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
}
