package com.ifpr.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:financeiro_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=segredo-exclusivo-de-testes-com-mais-de-32-bytes"
})
@AutoConfigureMockMvc
class BackendApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test void contextLoads() {}

    @Test void apiRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/wallets")).andExpect(status().isUnauthorized());
    }

    @Test void openApiDocumentsBudgetAndJwt() throws Exception {
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
            .andExpect(jsonPath("$.paths['/api/v1/wallets/{walletId}/budget'].get.security[0].bearerAuth").isArray());
    }

    @Test void ownerCanSaveBudgetAndSeeRealExpensesButOutsiderCannot() throws Exception {
        String owner = login("dono@example.com");
        String outsider = login("visitante@example.com");
        String walletBody = mvc.perform(post("/api/v1/wallets")
            .header("Authorization", owner).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("name", "Casa", "currency", "BRL"))))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long walletId = json.readTree(walletBody).get("id").asLong();
        LocalDate date = LocalDate.now().minusDays(1);
        String month = date.toString().substring(0, 7);
        String budget = "/api/v1/wallets/" + walletId + "/budget";
        mvc.perform(put(budget).param("month", month).header("Authorization", owner)
            .contentType(MediaType.APPLICATION_JSON).content("{\"limit\":100}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.limit").value(100));
        mvc.perform(post("/api/v1/wallets/" + walletId + "/transactions")
            .header("Authorization", owner).contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("type", "EXPENSE", "amount", 25,
                "date", date.toString(), "description", "Mercado"))))
            .andExpect(status().isCreated());
        mvc.perform(get(budget).param("month", month).header("Authorization", owner))
            .andExpect(status().isOk()).andExpect(jsonPath("$.spent").value(25))
            .andExpect(jsonPath("$.remaining").value(75));
        mvc.perform(get(budget).param("month", month).header("Authorization", outsider))
            .andExpect(status().isForbidden());
        mvc.perform(put(budget).param("month", month).header("Authorization", outsider)
            .contentType(MediaType.APPLICATION_JSON).content("{\"limit\":10}"))
            .andExpect(status().isForbidden());
        mvc.perform(put(budget).param("month", month).header("Authorization", owner)
            .contentType(MediaType.APPLICATION_JSON).content("{\"limit\":0}"))
            .andExpect(status().isBadRequest());
    }

    private String login(String email) throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("name", "Pessoa Teste", "email", email, "password", "SenhaTeste123"))))
            .andExpect(status().isCreated());
        String response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(Map.of("email", email, "password", "SenhaTeste123"))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(response).get("accessToken").asText();
    }
}
