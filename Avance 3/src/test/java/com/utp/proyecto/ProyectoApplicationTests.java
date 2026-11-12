package com.utp.proyecto;

import com.utp.proyecto.config.CatalogoDataInitializer;
import com.utp.proyecto.model.Rol;
import com.utp.proyecto.model.EstadoReserva;
import com.utp.proyecto.model.Reserva;
import com.utp.proyecto.model.ServicioTuristico;
import com.utp.proyecto.model.Usuario;
import com.utp.proyecto.repository.LugarTuristicoRepository;
import com.utp.proyecto.repository.ReservaRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    private ReservaRepository reservas;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private CatalogoDataInitializer initializer;
    @Autowired
    private ServicioTuristicoService servicioService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        assertThat(servicios.findAll()).allMatch(servicio -> servicio.getId() != null
                && Boolean.TRUE.equals(servicio.getAceptaReservas()));
        assertThat(lugares.findAll()).allMatch(lugar -> lugar.getPropietario() == null);
        assertThat(servicios.findAll()).allMatch(servicio -> servicio.getCreador() == null);
    }

    @Test
    @Transactional
    void persisteServicioConIdGeneradoYCreador() {
        Usuario guia = new Usuario();
        guia.setNombre("Propietario de prueba");
        guia.setEmail("propietario-prueba@example.test");
        guia.setPasswordHash("hash-de-prueba");
        guia.setRoles(Set.of(Rol.PROPIETARIO));
        guia = usuarios.save(guia);

        ServicioTuristico servicio = new ServicioTuristico();
        servicio.setNombre("Tour de prueba");
        servicio.setTipo("guia");
        servicio.setDescripcion("Servicio para validar la persistencia relacional.");
        servicio.setHorarios("Todos los días");
        servicio.setPrecio(new BigDecimal("75.50"));
        servicio.setRuta("Piura");
        servicio.setContacto("900000010");
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
                  "contacto": "900000011",
                  "aceptaReservas": true
                }
                """;

        String token = crearUsuarioConRol("propietario-servicios", Rol.PROPIETARIO);
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
    void lugaresValidanCategoriaTelefonoYUnicidadEntreRecursos() throws Exception {
        String tokenGuia = crearUsuarioConRol("guia-telefonos", Rol.GUIA);
        String tokenPropietario = crearUsuarioConRol("propietario-telefonos", Rol.PROPIETARIO);
        String lugar = """
                {
                  "nombre": "%s",
                  "tipo": "%s",
                  "descripcion": "Lugar para verificar la validación del teléfono",
                  "direccion": "Piura",
                  "horarios": "Todos los días",
                  "contacto": "%s"
                }
                """;

        String respuesta = mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(lugar.formatted("Playa con teléfono", "playa", "981234567")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long lugarId = ((Number) JsonPath.read(respuesta, "$.id")).longValue();

        mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(lugar.formatted("Tipo inválido", "naturaleza", "981234568")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(lugar.formatted("Teléfono inválido", "cultural", "12345678")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(lugar.formatted("Teléfono repetido", "cultural", "981234567")))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/lugares/{id}", lugarId)
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(lugar.formatted("Playa actualizada", "playa", "981234567")))
                .andExpect(status().isOk());

        String servicio = """
                {
                  "nombre": "Servicio con teléfono de lugar",
                  "tipo": "guia",
                  "descripcion": "Servicio para verificar unicidad global",
                  "horarios": "Todos los días",
                  "precio": 15.00,
                  "ruta": "Piura",
                  "contacto": "981234567",
                  "aceptaReservas": false
                }
                """;
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + tokenPropietario)
                        .contentType(APPLICATION_JSON)
                        .content(servicio))
                .andExpect(status().isConflict());

        String servicioPrimero = servicio.replace("981234567", "981234568");
        String respuestaServicio = mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + tokenPropietario)
                        .contentType(APPLICATION_JSON)
                        .content(servicioPrimero))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long servicioId = ((Number) JsonPath.read(respuestaServicio, "$.id")).longValue();
        mockMvc.perform(put("/api/servicios/{id}", servicioId)
                        .header("Authorization", "Bearer " + tokenPropietario)
                        .contentType(APPLICATION_JSON)
                        .content(servicioPrimero))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(lugar.formatted("Lugar con teléfono de servicio", "cultural", "981234568")))
                .andExpect(status().isConflict());
    }

    @Test
    void rutaDeCreacionRechazaPrecioFueraDePrecisionDecimal() throws Exception {
        String token = crearUsuarioConRol("propietario-precio-limite", Rol.PROPIETARIO);
        String payload = """
                {
                  "nombre": "Servicio con precio inválido",
                  "tipo": "guia",
                  "descripcion": "Prueba de límites de precio",
                  "horarios": "Todos los días",
                  "precio": %s,
                  "ruta": "Piura",
                  "contacto": "900000012",
                  "aceptaReservas": false
                }
                """;

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(payload.formatted("100000000.00")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(payload.formatted("1.239")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(payload.formatted("99999999.99")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precio").value(99999999.99));
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
                  "contacto": "900000001"
                }
                """;

        mockMvc.perform(get("/api/turista/lugares"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/lugares"))
                .andExpect(status().isOk());
        String tokenPropietario = crearUsuarioConRol("propietario-lugares", Rol.PROPIETARIO);
        mockMvc.perform(get("/api/estadisticas")
                        .header("Authorization", "Bearer " + tokenPropietario))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/servicios/estadisticas")
                        .header("Authorization", "Bearer " + tokenPropietario))
                .andExpect(status().isOk());
        String tokenGuia = crearUsuarioConRol("guia-lugares", Rol.GUIA);
        mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(lugar))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/servicios/lugares")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(lugar.replace("Lugar HTTP de prueba", "Lugar alias antiguo")
                                .replace("900000001", "900000002")))
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
                  "contacto": "900000017",
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
        mockMvc.perform(patch("/api/servicios/ruta-futura")
                        .header("Authorization", "Bearer " + tokenTurista))
                .andExpect(status().isForbidden());

        String tokenGuia = iniciarSesionConRol("guia-seguridad", Rol.GUIA);
        mockMvc.perform(post("/api/servicios/lugares")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Lugar no permitido a guía",
                                  "tipo": "cultural",
                                  "descripcion": "Validación de permisos del alias",
                                  "direccion": "Piura",
                                  "horarios": "Todos los días",
                                  "contacto": "900000003"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + tokenGuia)
                        .contentType(APPLICATION_JSON)
                        .content(payloadServicio))
                .andExpect(status().isForbidden());
        String tokenPropietario = iniciarSesionConRol("propietario-seguridad", Rol.PROPIETARIO);
        mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + tokenPropietario)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Lugar no permitido a propietario",
                                  "tipo": "playa",
                                  "descripcion": "Validación del permiso por rol",
                                  "direccion": "Piura",
                                  "horarios": "Todos los días",
                                  "contacto": "900000006"
                                }
                                """))
                .andExpect(status().isForbidden());
        String respuesta = mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + tokenPropietario)
                        .contentType(APPLICATION_JSON)
                        .content(payloadServicio))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number idRespuesta = JsonPath.read(respuesta, "$.id");
        long servicioId = idRespuesta.longValue();
        ServicioTuristico guardado = servicios.findById(servicioId).orElseThrow();
        assertThat(guardado.getCreador().getEmail()).contains("propietario-seguridad");
        mockMvc.perform(patch("/api/servicios/{id}/calificacion", servicioId)
                        .header("Authorization", "Bearer " + tokenPropietario)
                        .param("valor", "4"))
                .andExpect(status().isForbidden());

        String tokenOtroPropietario = iniciarSesionConRol("otro-propietario-seguridad", Rol.PROPIETARIO);
        mockMvc.perform(put("/api/servicios/{id}", servicioId)
                        .header("Authorization", "Bearer " + tokenOtroPropietario)
                        .contentType(APPLICATION_JSON)
                        .content(payloadServicio))
                .andExpect(status().isNotFound());

        assertThat(jwtService.obtenerSubject(tokenPropietario)).contains("propietario-seguridad");
    }

    @Test
    void rutasTuristicasSonPublicasSoloParaConsultaYProtegidasParaAcciones() throws Exception {
        Long lugarId = lugares.findAll().get(0).getId();
        Long servicioId = servicios.listarActivos().get(0).getId();

        mockMvc.perform(get("/api/turista/lugares"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/turista/servicios"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/turista/lugares/{id}", lugarId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/turista/servicios")
                        .param("tipo", "guia"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/turista/lugares/{id}/visitas", lugarId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/turista/lugares/{id}/calificacion", lugarId)
                        .param("valor", "4.5"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/turista/servicios/{id}/reservas", servicioId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/turista/reservas"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/turista/ruta-futura"))
                .andExpect(status().isUnauthorized());

        String tokenTurista = registrarUsuario("rutas-turista");
        mockMvc.perform(post("/api/turista/lugares/{id}/visitas", lugarId)
                        .header("Authorization", "Bearer " + tokenTurista))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/turista/lugares/{id}/calificacion", lugarId)
                        .header("Authorization", "Bearer " + tokenTurista)
                        .param("valor", "4.5"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/turista/servicios/{id}/reservas", servicioId)
                        .header("Authorization", "Bearer " + tokenTurista)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"fecha":"2099-07-10","cantidadPersonas":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        mockMvc.perform(get("/api/turista/reservas")
                        .header("Authorization", "Bearer " + tokenTurista))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/turista/ruta-futura")
                        .header("Authorization", "Bearer " + tokenTurista))
                .andExpect(status().isNotFound());

        String tokenGuia = iniciarSesionConRol("rutas-guia", Rol.GUIA);
        String tokenPropietario = iniciarSesionConRol("rutas-propietario", Rol.PROPIETARIO);
        for (String token : List.of(tokenGuia, tokenPropietario)) {
            mockMvc.perform(post("/api/turista/lugares/{id}/visitas", lugarId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
            mockMvc.perform(patch("/api/turista/lugares/{id}/calificacion", lugarId)
                            .header("Authorization", "Bearer " + token)
                            .param("valor", "4.5"))
                    .andExpect(status().isForbidden());
            mockMvc.perform(post("/api/turista/servicios/{id}/reservas", servicioId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/turista/reservas")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/turista/ruta-futura")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void turistaPuedeCalificarServiciosYLugaresYSePersisteLaCalificacion() throws Exception {
            String tokenTurista = registrarUsuario("turista-califica");
            long servicioId = servicios.listarActivos().get(0).getId();
            long lugarId = lugares.findAll().get(0).getId();

            mockMvc.perform(patch("/api/servicios/{id}/calificacion", servicioId)
                            .header("Authorization", "Bearer " + tokenTurista)
                            .param("valor", "4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.calificacion").value(4.0));
            mockMvc.perform(patch("/api/lugares/{id}/calificacion", lugarId)
                            .header("Authorization", "Bearer " + tokenTurista)
                            .param("valor", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.calificacion").value(3.0));

            assertThat(servicios.findById(servicioId).orElseThrow().getCalificacion()).isEqualTo(4.0);
            assertThat(lugares.findById(lugarId).orElseThrow().getCalificacion()).isEqualTo(3.0);
            mockMvc.perform(patch("/api/servicios/{id}/calificacion", servicioId)
                            .header("Authorization", "Bearer " + tokenTurista)
                            .param("valor", "6"))
                    .andExpect(status().isBadRequest());
        }

    @Test
    void turistaPuedeSolicitarReservaParaUnLugarPublicado() throws Exception {
            String tokenGuia = crearUsuarioConRol("guia-lugar-reservable", Rol.GUIA);
            String lugarJson = """
                    {
                      "nombre": "Lugar reservable",
                      "tipo": "playa",
                      "descripcion": "Lugar de prueba para reservas",
                      "direccion": "Piura",
                      "horarios": "Todos los días",
                      "contacto": "900000007"
                    }
                    """;
            String lugarResponse = mockMvc.perform(post("/api/lugares")
                            .header("Authorization", "Bearer " + tokenGuia)
                            .contentType(APPLICATION_JSON)
                            .content(lugarJson))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            long lugarId = ((Number) JsonPath.read(lugarResponse, "$.id")).longValue();
            String tokenTurista = registrarUsuario("turista-reserva-lugar");

            String reservation = mockMvc.perform(post("/api/turista/lugares/{id}/reservas", lugarId)
                            .header("Authorization", "Bearer " + tokenTurista)
                            .contentType(APPLICATION_JSON)
                            .content("{\"fecha\":\"2099-07-10\",\"cantidadPersonas\":2}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.lugarId").value(lugarId))
                    .andExpect(jsonPath("$.lugarNombre").value("Lugar reservable"))
                    .andExpect(jsonPath("$.servicioId").doesNotExist())
                    .andReturn().getResponse().getContentAsString();
            long reservaId = ((Number) JsonPath.read(reservation, "$.id")).longValue();

            mockMvc.perform(get("/api/turista/reservas")
                            .header("Authorization", "Bearer " + tokenTurista))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].lugarNombre").value("Lugar reservable"));
            mockMvc.perform(get("/api/servicios/reservas")
                            .header("Authorization", "Bearer " + tokenGuia))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(reservaId));
            mockMvc.perform(patch("/api/servicios/reservas/{id}/estado", reservaId)
                            .header("Authorization", "Bearer " + tokenGuia)
                            .contentType(APPLICATION_JSON)
                            .content("{\"estado\":\"CONFIRMADA\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    void preflightCorsPermiteElOrigenAngularConfigurado() throws Exception {
        mockMvc.perform(options("/api/servicios")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsString("authorization")));

        mockMvc.perform(options("/api/servicios")
                        .header("Origin", "http://127.0.0.1:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:4200"));
    }

    @Test
    void rutasDeGestionListanSoloRegistrosDelUsuarioAutenticado() throws Exception {
        String propietarioA = iniciarSesionConRol("propietario-listado-a", Rol.PROPIETARIO);
        String propietarioB = iniciarSesionConRol("propietario-listado-b", Rol.PROPIETARIO);
        String guia = iniciarSesionConRol("guia-listado", Rol.GUIA);
        String servicioBase = """
                {
                  "nombre": "%s",
                  "tipo": "guia",
                  "descripcion": "Servicio para probar aislamiento de gestión",
                  "horarios": "Todos los días",
                  "precio": 25.00,
                  "ruta": "Piura",
                  "contacto": "%s",
                  "aceptaReservas": true
                }
                """;

        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + propietarioA)
                        .contentType(APPLICATION_JSON)
                        .content(servicioBase.formatted("Servicio privado A", "900000013")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/servicios")
                        .header("Authorization", "Bearer " + propietarioB)
                        .contentType(APPLICATION_JSON)
                        .content(servicioBase.formatted("Servicio privado B", "900000014")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/servicios/mios")
                        .header("Authorization", "Bearer " + propietarioA))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Servicio privado A")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Servicio privado B"))));

        String lugar = """
                {
                  "nombre": "%s",
                  "tipo": "cultural",
                  "descripcion": "Lugar para probar el listado del propietario",
                  "direccion": "Piura",
                  "horarios": "Todos los días",
                  "contacto": "900000004"
                }
                """;
        mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + guia)
                        .contentType(APPLICATION_JSON)
                        .content(lugar.formatted("Lugar privado propietario")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/servicios/lugares")
                        .header("Authorization", "Bearer " + guia))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Lugar privado propietario")));
    }

    @Test
    void turistaCreaReservaYPropietarioLaConfirmaSinIncrementarAtenciones() throws Exception {
            String tokenPropietario = iniciarSesionConRol("dueno-reserva", Rol.PROPIETARIO);
            String servicioJson = """
                    {
                      "nombre": "Servicio reservable de prueba",
                      "tipo": "guia",
                      "descripcion": "Servicio creado para probar solicitudes de reserva",
                      "horarios": "Todos los días",
                      "precio": 50.00,
                      "ruta": "Piura",
                      "contacto": "900000015",
                      "aceptaReservas": true
                    }
                    """;
            String servicioRespuesta = mockMvc.perform(post("/api/servicios")
                            .header("Authorization", "Bearer " + tokenPropietario)
                            .contentType(APPLICATION_JSON)
                            .content(servicioJson))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            long servicioId = ((Number) JsonPath.read(servicioRespuesta, "$.id")).longValue();

            String tokenTurista = registrarUsuario("solicita-reserva");
            String reservaJson = """
                    {"fecha":"2099-07-10","cantidadPersonas":2}
                    """;
            String reservaRespuesta = mockMvc.perform(post("/api/turista/servicios/{id}/reservas", servicioId)
                            .header("Authorization", "Bearer " + tokenTurista)
                            .contentType(APPLICATION_JSON)
                            .content(reservaJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                    .andExpect(jsonPath("$.cantidadPersonas").value(2))
                    .andReturn().getResponse().getContentAsString();
            long reservaId = ((Number) JsonPath.read(reservaRespuesta, "$.id")).longValue();
            assertThat(servicios.findById(servicioId).orElseThrow().getAtenciones()).isZero();
            mockMvc.perform(delete("/api/servicios/{id}", servicioId)
                            .header("Authorization", "Bearer " + tokenPropietario))
                    .andExpect(status().isConflict());

            String tokenOtroPropietario = iniciarSesionConRol("otro-dueno-reserva", Rol.PROPIETARIO);
            mockMvc.perform(patch("/api/servicios/reservas/{id}/estado", reservaId)
                            .header("Authorization", "Bearer " + tokenOtroPropietario)
                            .contentType(APPLICATION_JSON)
                            .content("{\"estado\":\"CONFIRMADA\"}"))
                    .andExpect(status().isNotFound());
            String tokenOtroTurista = registrarUsuario("otro-turista-reserva");
            mockMvc.perform(delete("/api/turista/reservas/{id}", reservaId)
                            .header("Authorization", "Bearer " + tokenOtroTurista))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/servicios/reservas")
                            .header("Authorization", "Bearer " + tokenPropietario))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(reservaId));
            mockMvc.perform(get("/api/turista/reservas")
                            .header("Authorization", "Bearer " + tokenTurista))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));

            mockMvc.perform(patch("/api/servicios/reservas/{id}/estado", reservaId)
                            .header("Authorization", "Bearer " + tokenPropietario)
                            .contentType(APPLICATION_JSON)
                            .content("{\"estado\":\"CONFIRMADA\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
            mockMvc.perform(delete("/api/turista/reservas/{id}", reservaId)
                            .header("Authorization", "Bearer " + tokenTurista))
                    .andExpect(status().isConflict());
            assertThat(reservas.findById(reservaId)).get()
                    .extracting(Reserva::getEstado)
                    .isEqualTo(EstadoReserva.CONFIRMADA);
        }

    @Test
    void turistaPuedeCancelarSoloSuSolicitudPendienteYSeValidanLosDatos() throws Exception {
            String tokenPropietario = iniciarSesionConRol("dueno-cancelacion", Rol.PROPIETARIO);
            String payloadServicio = """
                    {
                      "nombre": "Servicio para cancelación",
                      "tipo": "guia",
                      "descripcion": "Servicio creado para probar cancelación",
                      "horarios": "Todos los días",
                      "precio": 30.00,
                      "ruta": "Piura",
                      "contacto": "900000016",
                      "aceptaReservas": true
                    }
                    """;
            String respuestaServicio = mockMvc.perform(post("/api/servicios")
                            .header("Authorization", "Bearer " + tokenPropietario)
                            .contentType(APPLICATION_JSON)
                            .content(payloadServicio))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            long servicioId = ((Number) JsonPath.read(respuestaServicio, "$.id")).longValue();
            String tokenTurista = registrarUsuario("turista-cancela");
            String respuestaSinReservas = mockMvc.perform(post("/api/servicios")
                            .header("Authorization", "Bearer " + tokenPropietario)
                            .contentType(APPLICATION_JSON)
                            .content(payloadServicio
                                    .replace("Servicio para cancelación", "Servicio sin reservas")
                                    .replace("900000016", "900000018")
                                    .replace("\"aceptaReservas\": true", "\"aceptaReservas\": false")))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            long servicioSinReservasId = ((Number) JsonPath.read(respuestaSinReservas, "$.id")).longValue();
            mockMvc.perform(post("/api/turista/servicios/{id}/reservas", servicioSinReservasId)
                            .header("Authorization", "Bearer " + tokenTurista)
                            .contentType(APPLICATION_JSON)
                            .content("{\"fecha\":\"2099-07-10\",\"cantidadPersonas\":1}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.estado").value("PENDIENTE"));

            for (String payload : List.of(
                    "{\"fecha\":\"2000-01-01\",\"cantidadPersonas\":1}",
                    "{\"fecha\":\"2099-07-10\",\"cantidadPersonas\":0}")) {
                mockMvc.perform(post("/api/turista/servicios/{id}/reservas", servicioId)
                                .header("Authorization", "Bearer " + tokenTurista)
                                .contentType(APPLICATION_JSON)
                                .content(payload))
                        .andExpect(status().isBadRequest());
            }

            String respuestaReserva = mockMvc.perform(post("/api/turista/servicios/{id}/reservas", servicioId)
                            .header("Authorization", "Bearer " + tokenTurista)
                            .contentType(APPLICATION_JSON)
                            .content("{\"fecha\":\"2099-07-10\",\"cantidadPersonas\":1}"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            long reservaId = ((Number) JsonPath.read(respuestaReserva, "$.id")).longValue();

            mockMvc.perform(delete("/api/turista/reservas/{id}", reservaId)
                            .header("Authorization", "Bearer " + tokenTurista))
                    .andExpect(status().isNoContent());
            assertThat(reservas.findById(reservaId)).get()
                    .extracting(Reserva::getEstado)
                    .isEqualTo(EstadoReserva.CANCELADA);

            mockMvc.perform(delete("/api/turista/reservas/{id}", reservaId)
                            .header("Authorization", "Bearer " + tokenTurista))
                    .andExpect(status().isConflict());
    }

    @Test
    void registroUsaBCryptYConservaAliasRegister() throws Exception {
        String password = "Password123!";
        String registroNuevo = """
                {"nombre":"Registro nuevo","email":"registro-nuevo@example.test","password":"%s"}
                """.formatted(password);
        String respuesta = mockMvc.perform(post("/api/auth/registro")
                        .contentType(APPLICATION_JSON)
                        .content(registroNuevo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("TURISTA"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        assertThat(JsonPath.<String>read(respuesta, "$.token")).isNotBlank();

        Usuario usuario = usuarios.findByEmailIgnoreCase("registro-nuevo@example.test").orElseThrow();
        assertThat(usuario.getPasswordHash()).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, usuario.getPasswordHash())).isTrue();

        String registroAnterior = """
                {"nombre":"Registro anterior","email":"registro-anterior@example.test","password":"%s"}
                """.formatted(password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registroAnterior))
                .andExpect(status().isCreated());
    }

    @Test
    void cuentaPuedeAgregarSoloRolesDeProveedorYUsarElMismoToken() throws Exception {
        mockMvc.perform(post("/api/cuenta/roles")
                        .contentType(APPLICATION_JSON)
                        .content("{\"rol\":\"GUIA\"}"))
                .andExpect(status().isUnauthorized());

        String token = registrarUsuario("solicitud-rol");
        mockMvc.perform(post("/api/cuenta/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"rol\":\"GUIA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.containsInAnyOrder("TURISTA", "GUIA")));

        Usuario usuario = usuarios.findByEmailIgnoreCase("solicitud-rol@example.test").orElseThrow();
        assertThat(usuario.getRoles()).containsExactlyInAnyOrder(Rol.TURISTA, Rol.GUIA);

        mockMvc.perform(get("/api/servicios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/servicios/lugares")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cuenta/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"rol\":\"GUIA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.containsInAnyOrder("TURISTA", "GUIA")));

        mockMvc.perform(post("/api/cuenta/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"rol\":\"PROPIETARIO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles",
                        org.hamcrest.Matchers.containsInAnyOrder("TURISTA", "GUIA", "PROPIETARIO")));
        assertThat(usuarios.findByEmailIgnoreCase("solicitud-rol@example.test").orElseThrow().getRoles())
                .containsExactlyInAnyOrder(Rol.TURISTA, Rol.GUIA, Rol.PROPIETARIO);
        mockMvc.perform(get("/api/servicios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/lugares")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Lugar del propietario",
                                  "tipo": "cultural",
                                  "descripcion": "Lugar creado tras agregar rol",
                                  "direccion": "Piura",
                                  "horarios": "Todos los días",
                                  "contacto": "900000005"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void solicitudDeRolRechazaTuristaYRolesDesconocidos() throws Exception {
        String token = registrarUsuario("rol-invalido");
        mockMvc.perform(post("/api/cuenta/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"rol\":\"TURISTA\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/cuenta/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{\"rol\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/cuenta/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
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
        String respuesta = mockMvc.perform(post("/api/auth/registro")
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
