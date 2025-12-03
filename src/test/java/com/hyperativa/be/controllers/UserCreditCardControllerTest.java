package com.hyperativa.be.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperativa.be.dtos.CreditCardRequest;
import com.hyperativa.be.exceptions.CreditCardException;
import com.hyperativa.be.services.creditcard.CreditCardBatchService;
import com.hyperativa.be.services.creditcard.UserCreditCardService;
import com.hyperativa.be.util.LoggedUsernameSupplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc(addFilters = false)
class UserCreditCardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoggedUsernameSupplier loggedUsernameSupplier;

    @MockitoBean
    private UserCreditCardService userCreditCardService;

    @MockitoBean
    private CreditCardBatchService creditCardBatchService;

    @Test
    void testAddCreditCard_success() throws Exception {
        var username = "testUser";
        var cardId = UUID.randomUUID();
        var request = new CreditCardRequest("4111111111111111");

        when(loggedUsernameSupplier.get()).thenReturn(username);
        when(userCreditCardService.registerCreditCard(any())).thenReturn(cardId);

        mockMvc.perform(post("/credit-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(cardId.toString()));

        verify(userCreditCardService).registerCreditCard(any());
    }

    @Test
    void testFindCreditCard_success() throws Exception {
        var username = "testUser";
        var cardId = UUID.randomUUID();
        var request = new CreditCardRequest("4111111111111111");

        when(loggedUsernameSupplier.get()).thenReturn(username);
        when(userCreditCardService.checkUserCreditCard(any())).thenReturn(cardId);

        mockMvc.perform(post("/credit-cards/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(cardId.toString()));

        verify(userCreditCardService).checkUserCreditCard(any());
    }

    @Test
    void testUploadBatch_success() throws Exception {
        var username = "testUser";
        var fileContent = "DESAFIO-HYPERATIVA\nC1 4111111111111111\nLOTE0001000001";
        var multipartFile = new MockMultipartFile("file", "batch.txt", "text/plain", fileContent.getBytes());

        when(loggedUsernameSupplier.get()).thenReturn(username);

        mockMvc.perform(multipart("/credit-cards/upload")
                        .file(multipartFile))
                .andExpect(status().isCreated());

        verify(creditCardBatchService).processFile(any());
    }

    @Test
    void testUploadBatch_emptyFile_throwsValidationException() throws Exception {
        var username = "testUser";
        var multipartFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        when(loggedUsernameSupplier.get()).thenReturn(username);

        mockMvc.perform(multipart("/credit-cards/upload")
                        .file(multipartFile))
                .andExpect(status().isBadRequest());

        verify(creditCardBatchService, never()).processFile(any());
    }

    @Test
    void testUploadBatch_422() throws Exception {
        var fileContent = "DESAFIO-HYPERATIVA\nC2 4111111111111111\nC2 4111111111111111\nLOTE0001000002";
        var multipartFile = new MockMultipartFile("file", "batch.txt", "text/plain", fileContent.getBytes());

        doThrow(new CreditCardException("any")).when(creditCardBatchService).processFile(any());

        mockMvc.perform(multipart("/credit-cards/upload").file(multipartFile))
                .andExpect(status().isUnprocessableEntity());
    }
}