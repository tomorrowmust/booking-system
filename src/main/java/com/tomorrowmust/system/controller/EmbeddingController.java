package com.tomorrowmust.system.controller;

import cn.hutool.core.collection.CollStreamUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/embedding")
@Tag(name = "向量数据库管理", description = "向量数据库管理相关接口")
@RequiredArgsConstructor
public class EmbeddingController {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    @PostMapping
    @Operation(summary = "保存数据到向量数据库", description = "保存数据到向量数据库")
    public void embedding(@RequestParam("messages")List<String>messages) {
        log.info("保存数据到向量数据库 {}", messages);
        List<Document> documents = CollStreamUtil.toList(messages, message -> Document.builder()
                .text(message)
                .build());
        vectorStore.add(documents);
        log.info("保存数据到向量数据库 数量{}", messages.size());
    }
    @GetMapping
    @Operation(summary = "对数据进行向量化", description = "对数据进行向量化")
    public EmbeddingResponse embed(@RequestParam("message") String message) {
        return embeddingModel.embedForResponse(List.of(message));
    }

    @DeleteMapping
    @Operation(summary = "删除向量数据库中的数据", description = "删除向量数据库中的数据")
    public void deleteVectorStore(@RequestParam("ids") List<String> ids) {
        // 删除向量数据库中的数据
        vectorStore.delete(ids);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索向量数据库", description = "搜索向量数据库")
    public List<Document> search(@RequestParam("message") String message) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(5).build());
    }

    @GetMapping("/search/all")
    @Operation(summary = "搜索向量数据库中的所有数据", description = "搜索向量数据库中的所有数据")
    public List<Document> searchAll() {
        // 搜索全部数据
        return vectorStore.similaritySearch(SearchRequest.builder().query("").topK(999).build());
    }

}
