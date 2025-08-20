package com.notabene.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.notabene.dto.CreateTagRequest;
import com.notabene.dto.TagDTO;
import com.notabene.service.TagService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService service;

    @GetMapping
    public List<TagDTO> list(@RequestParam(required = false) String q) {
        return service.list(q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagDTO create(@Valid @RequestBody CreateTagRequest req, Authentication auth) {
        // Con il tuo filtro il principal è una String; se non ti serve, lascia null
        return service.create(req.name(), null);
    }
}



