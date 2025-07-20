package svs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import svs.command.dto.CommandDto;
import svs.command.mapper.CommandMapper;
import svs.command.service.CommandService;

@RestController
@RequestMapping("/commands")
@RequiredArgsConstructor
@Slf4j
public class CommandController {

    private final CommandService commandService;

    @PostMapping
    public ResponseEntity<String> submitCommand(@Valid @RequestBody CommandDto commandDto) {
        log.info("submitCommand received");
        commandService.executeCommand(CommandMapper.toCommand(commandDto));
        return ResponseEntity.ok("Command accepted");
    }
}