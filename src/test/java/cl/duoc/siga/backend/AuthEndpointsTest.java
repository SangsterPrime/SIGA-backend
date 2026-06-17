package cl.duoc.siga.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de los endpoints de autenticación manual sobre H2.
 * Cubre los escenarios mínimos del enunciado: registro PASAJERO/FUNCIONARIO,
 * validación del código de funcionario, email duplicado, login y password incorrecta,
 * y que las respuestas nunca incluyan el hash de la contraseña.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthEndpointsTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void registroPasajero_ok_sinPasswordHash() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Pasajero Uno","email":"pasajero1@test.cl","password":"secret12","role":"PASAJERO"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("pasajero1@test.cl"))
                .andExpect(jsonPath("$.user.role").value("PASAJERO"))
                .andExpect(jsonPath("$.user.provider").value("LOCAL"))
                .andExpect(content().string(not(containsStringIgnoringCase("passwordHash"))))
                .andExpect(content().string(not(containsStringIgnoringCase("password_hash"))));
    }

    @Test
    void registroFuncionario_codigoCorrecto_ok() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Func Bueno","email":"func-ok@test.cl","password":"secret12","role":"FUNCIONARIO","employeeCode":"SIGA-FUNC-2026"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.user.provider").value("LOCAL"));
    }

    @Test
    void registroFuncionario_codigoIncorrecto_falla() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Func Malo","email":"func-malo@test.cl","password":"secret12","role":"FUNCIONARIO","employeeCode":"NOPE"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void registroEmailDuplicado_falla() throws Exception {
        String body = """
                {"name":"Dup","email":"dup@test.cl","password":"secret12","role":"PASAJERO"}""";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_ok_y_passwordIncorrecta_falla() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Login User","email":"login@test.cl","password":"secret12","role":"PASAJERO"}"""))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"login@test.cl","password":"secret12"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("login@test.cl"))
                .andExpect(jsonPath("$.user.provider").value("LOCAL"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email":"login@test.cl","password":"WRONGpass"}"""))
                .andExpect(status().isUnauthorized());
    }
}
