package svs.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import svs.command.model.Command;
import svs.command.service.CommandService;
import svs.exception.CommandQueueOverflowException;
import svs.handler.ExceptionsHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommandControllerTest {

    @Mock
    private CommandService commandService;

    @InjectMocks
    private CommandController commandController;

    private MockMvc mockMvc;

    private final String VALID_JSON = """
        {
          "description": "Scan area",
          "priority": "COMMON",
          "author": "Bishop",
          "time": "2025-07-25T12:05:00"
        }
        """;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(commandController)
                .setControllerAdvice(new ExceptionsHandler())
                .build();
    }

    @Test
    void submitCommand_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Command accepted"));
    }

    @Test
    void submitCommand_ShouldReturnValidationError() throws Exception {
        String invalidJson = """
            {
              "description": "",
              "priority": "COMMON",
              "author": "",
              "time": "invalid-time"
            }
            """;

        mockMvc.perform(post("/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ValidationError"));
    }

    @Test
    void submitCommand_ShouldReturnIllegalArgumentError() throws Exception {
        doThrow(new IllegalArgumentException("Invalid priority value"))
                .when(commandService).executeCommand(any(Command.class));

        mockMvc.perform(post("/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BadRequest"))
                .andExpect(jsonPath("$.message").value("Invalid priority value"));
    }

    @Test
    void submitCommand_ShouldReturnQueueOverflowError() throws Exception {
        doThrow(new CommandQueueOverflowException("Queue is full"))
                .when(commandService).executeCommand(any(Command.class));

        mockMvc.perform(post("/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("QueueOverflow"))
                .andExpect(jsonPath("$.message").value("Queue is full"));
    }

    @Test
    void submitCommand_ShouldReturnInternalError() throws Exception {
        doThrow(new RuntimeException("Unexpected error"))
                .when(commandService).executeCommand(any(Command.class));

        mockMvc.perform(post("/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("InternalError"))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }
}