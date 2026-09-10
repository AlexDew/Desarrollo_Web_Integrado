package com.utp.proyecto.controller;

import com.utp.proyecto.model.LugarTuristico;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.service.CatalogoTuristicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServiciosController {
    private final CatalogoTuristicoService catalogo;
    public ServiciosController(CatalogoTuristicoService catalogo) { this.catalogo = catalogo; }

    @PostMapping("/lugares")
    @ResponseStatus(HttpStatus.CREATED)
    public LugarTuristico registrarLugar(@RequestBody LugarTuristico lugar) { return catalogo.guardarLugar(lugar); }
    @PutMapping("/lugares/{id}")
    public ResponseEntity<LugarTuristico> actualizarLugar(@PathVariable Long id, @RequestBody LugarTuristico lugar) {
        return ResponseEntity.ofNullable(catalogo.actualizarLugar(id, lugar));
    }
    @DeleteMapping("/lugares/{id}")
    public ResponseEntity<Void> eliminarLugar(@PathVariable Long id) {
        return catalogo.eliminarLugar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioTuristico registrarServicio(@RequestBody ServicioTuristico servicio) { return catalogo.guardarServicio(servicio); }
    @PutMapping("/{id}")
    public ResponseEntity<ServicioTuristico> actualizarServicio(@PathVariable Long id, @RequestBody ServicioTuristico servicio) {
        return ResponseEntity.ofNullable(catalogo.actualizarServicio(id, servicio));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long id) {
        return catalogo.eliminarServicio(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    @GetMapping
    public List<ServicioTuristico> listarServicios() { return catalogo.listarServicios(); }
    @GetMapping("/estadisticas")
    public Object estadisticas() {
        return java.util.Map.of("lugaresRegistrados", catalogo.listarLugares().size(),
                "serviciosRegistrados", catalogo.listarServicios().size());
    }
}
