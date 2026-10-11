package com.utp.proyecto;

import com.utp.proyecto.config.CatalogoDataInitializer;
import com.utp.proyecto.model.Rol;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.LugarTuristicoRepository;
import com.utp.proyecto.repository.ServicioTuristicoRepository;
import com.utp.proyecto.repository.UsuarioRepository;
import com.utp.proyecto.security.JwtService;
import com.utp.proyecto.service.ServicioTuristicoService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ProyectoApplicationTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    @Autowired
    private LugarTuristicoRepository lugares;
    @Autowired
    private ServicioTuristicoRepository servicios;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private CatalogoDataInitializer initializer;
    @Autowired
    private ServicioTuristicoService servicioService;
    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

	@Test
	void contextLoads() {
	}

    @Test
    void importaLosCatalogosInicialesYEsIdempotente() {
        long cantidadLugares = lugares.count();
        long cantidadServicios = servicios.count();

        assertThat(cantidadLugares).isPositive();
        assertThat(cantidadServicios).isPositive();

        initializer.run(new DefaultApplicationArguments());

        assertThat(lugares.count()).isEqualTo(cantidadLugares);
        assertThat(servicios.count()).isEqualTo(cantidadServicios);
        assertThat(lugares.findAll()).allMatch(lugar -> lugar.getId() != null);
        assertThat(servicios.findAll()).allMatch(servicio -> servicio.getId() != null);
        assertThat(lugares.findAll()).allMatch(lugar -> lugar.getPropietario() == null);
        assertThat(servicios.findAll()).allMatch(servicio -> servicio.getCreador() == null);
    }

    @Test
    @Transactional
    void persisteServicioConIdGeneradoYCreador() {
        Usuario guia = new Usuario();
        guia.setNombre("Guía de prueba");
        guia.setEmail("guia-prueba@example.test");
        guia.setPasswordHash("hash-de-prueba");
        guia.setRoles(Set.of(Rol.GUIA));
        guia = usuarios.save(guia);

        ServicioTuristico servicio = new ServicioTuristico();
        servicio.setNombre("Tour de prueba");
        servicio.setTipo("guia");
        servicio.setDescripcion("Servicio para validar la persistencia relacional.");
        servicio.setHorarios("Todos los días");
        servicio.setPrecio(new BigDecimal("75.50"));
        servicio.setRuta("Piura");
        servicio.setContacto("guia@example.test");
        servicio.setAceptaReservas(true);
        servicio.setCreador(guia);

        ServicioTuristico guardado = servicioService.guardarServicio(servicio, guia.getId());

        assertThat(guardado.getId()).isNotNull();
        assertThat(servicios.findById(guardado.getId())).isPresent()
                .get()
                .extracting(ServicioTuristico::getNombre)
                .isEqualTo("Tour de prueba");
        assertThat(guardado.getCreador().getId()).isEqualTo(guia.getId());
    }

    @Test
    void rutaDeCreacionValidaCamposYPersisteServicio() throws Exception {
        String payload = """
                {
                  "nombre": "Servicio HTTP de prueba",
                  "tipo": "guia",
                  "descripcion": "Registro desde la ruta REST",
                  "horarios": "Todos los días",
                  "precio": 40.50,
                  "ruta": "Piura",
                  "contacto": "contacto@example.test",
                  "aceptaReservas": true
                }
                """;

        String token = crearUsuarioConRol("guia-servicios", Rol.GUIA);
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(get("/api/turista/servicios"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Servicio HTTP de prueba")));

        mockMvc.perform(post("/api/servicios")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rutasDeLugaresYEstadisticasMantienenCompatibilidad() throws Exception {
        String lugar = """
                {
                  "nombre": "Lugar HTTP de prueba",
                  "tipo": "cultural",
                  "descripcion": "Registro desde la ruta REST",
                  "direccion": "Centro de Piura",
                  "horarios": "Todos los días",
                  "contacto": "contacto@example.test"
                }
                """;

        mockMvc.perform(get("/api/turista/lugares"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/lugares"))
                .andExpect(status().isOk());
        String token = crearUsuarioConRol("propietario-lugares", Rol.PROPIETARIO);
        mockMvc.perform(get("/api/estadisticas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/servicios/estadisticas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(lugar))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/servicios/lugares")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(lugar.replace("Lugar HTTP de prueba", "Lugar alias antiguo")))
                .andExpect(status().isCreated());
    }

    @Test
    void seguridadJwtAplicaAutenticacionRolesYPropiedad() throws Exception {
        String payloadServicio = """
                {
                  "nombre": "Servicio protegido",
                  "tipo": "guia",
                  "descripcion": "Servicio para validar permisos",
                  "horarios": "Todos los días",
                  "precio": 40.50,
                  "ruta": "Piura",
                  "contacto": "contacto@example.test",
                  "aceptaReservas": true
                }
                """;
        mockMvc.perform(get("/api/turista/servicios"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/servicios")
                        .contentType(APPLICATION_JSON)
                        .content(payloadServicio))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/turista/servicios")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());

        String tokenTurista = registrarUsuario("turista-seguridad");
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + tokenTurista)
                        .contentType(APPLICATION_JSON)
                        .content(payloadServicio))
                .andExpect(status().isForbidden());

        String tokenGuia = iniciarSesionConRol("guia-seguridad", Rol.GUIA);
        String respuesta = mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(payloadServicio))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number idRespuesta = JsonPath.read(respuesta, "$.id");
        long servicioId = idRespuesta.longValue();
        ServicioTuristico guardado = servicios.findById(servicioId).orElseThrow();
        assertThat(guardado.getCreador().getEmail()).contains("guia-seguridad");

        String tokenOtroGuia = iniciarSesionConRol("otro-guia-seguridad", Rol.GUIA);
        mockMvc.perform(put("/api/servicios/{id}", servicioId)
                        .header("Authorization", "Bearer " + tokenOtroGuia)
                        .contentType(APPLICATION_JSON)
                        .content(payloadServicio))
                .andExpect(status().isNotFound());

        assertThat(jwtService.obtenerSubject(tokenGuia)).contains("guia-seguridad");
    }

    private String crearUsuarioConRol(String nombreUsuario, Rol rol) throws Exception {
        registrarUsuario(nombreUsuario);
        Usuario usuario = usuarios.findByEmailIgnoreCase(nombreUsuario + "@example.test").orElseThrow();
        usuario.getRoles().clear();
        usuario.getRoles().add(rol);
        usuarios.save(usuario);
        return iniciarSesion(nombreUsuario);
    }

    private String iniciarSesionConRol(String nombreUsuario, Rol rol) throws Exception {
        registrarUsuario(nombreUsuario);
        Usuario usuario = usuarios.findByEmailIgnoreCase(nombreUsuario + "@example.test").orElseThrow();
        usuario.getRoles().clear();
        usuario.getRoles().add(rol);
        usuarios.save(usuario);
        return iniciarSesion(nombreUsuario);
    }

    private String registrarUsuario(String nombreUsuario) throws Exception {
        String payload = """
                {"nombre":"%s","email":"%s@example.test","password":"Password123!"}
                """.formatted(nombreUsuario, nombreUsuario);
        String respuesta = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private String iniciarSesion(String nombreUsuario) throws Exception {
        String payload = """
                {"email":"%s@example.test","password":"Password123!"}
                """.formatted(nombreUsuario);
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }
}
