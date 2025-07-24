package com.plataformanegocios.sufix.ingestaoservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ingestao")
public class IngestaoController {

    @GetMapping("/status")
    public Mono<String> getStatus() {
        return Mono.just("Ingestao Service está no ar e funcionando!");
    }
}