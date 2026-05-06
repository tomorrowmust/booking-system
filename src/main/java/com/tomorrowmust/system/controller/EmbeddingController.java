package com.tomorrowmust.system.controller;

import cn.hutool.core.collection.CollStreamUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@Slf4j
@RestController
@RequestMapping("/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

    private final VectorStore vectorStore;

    @PostMapping
    public void embedding(@RequestParam("messages")List<String>messages) {
        log.info("保存数据到向量数据库 {}", messages);
        List<Document> documents = CollStreamUtil.toList(messages, message -> Document.builder()
                .text(message)
                .build());
        vectorStore.add(documents);
        log.info("保存数据到向量数据库 数量{}", messages.size());
    }
}
