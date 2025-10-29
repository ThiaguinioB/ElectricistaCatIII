package com.electricistacat.backend.web;

import com.electricistacat.backend.IntegrationTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectWorkflowIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateProjectPanelCircuitAndGenerateBom() throws Exception {
        String projectPayload = """
                {
                  \"name\": \"Obra Test\",
                  \"clientName\": \"Cooperativa\",
                  \"address\": \"Córdoba\",
                  \"description\": \"Instalación residencial\",
                  \"voltageSystemType\": \"SINGLE_PHASE\"
                }
                """;

        String projectJson = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode projectNode = objectMapper.readTree(projectJson);
        Long projectId = projectNode.get("id").asLong();
        assertThat(projectId).isNotNull();

        String panelPayload = """
                {
                  \"name\": \"Tablero Principal\",
                  \"panelType\": \"MAIN\",
                  \"dinRailCount\": 2
                }
                """;

        String panelJson = mockMvc.perform(post("/api/projects/" + projectId + "/panels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(panelPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode panelNode = objectMapper.readTree(panelJson);
        Long panelId = panelNode.get("id").asLong();
        assertThat(panelId).isNotNull();

        String circuitPayload = """
                {
                  \"name\": \"Iluminacion Planta Baja\",
                  \"loadType\": \"ILUMINACION\",
                  \"demandPowerKw\": 3.0,
                  \"voltage\": 230,
                  \"cosPhi\": 0.9,
                  \"efficiency\": 0.9,
                  \"phaseType\": \"SINGLE_PHASE\",
                  \"lengthMeters\": 20,
                  \"conductorCrossSection\": 2.5,
                  \"conductorMaterial\": \"CU\",
                  \"installationMethod\": \"concealed_conduit\",
                  \"groupingFactorCount\": 1,
                  \"ambientTemperature\": 30,
                  \"lighting\": true
                }
                """;

        String circuitJson = mockMvc.perform(post("/api/panels/" + panelId + "/circuits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(circuitPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode circuitNode = objectMapper.readTree(circuitJson);
        assertThat(circuitNode.get("designCurrent").asDouble()).isGreaterThan(0);

        mockMvc.perform(get("/api/projects/" + projectId + "/bom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        mockMvc.perform(post("/api/calculations/ia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"powerKw\":4.5," +
                                "\"voltage\":230," +
                                "\"powerFactor\":0.9," +
                                "\"efficiency\":0.92" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        mockMvc.perform(post("/api/projects/" + projectId + "/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
