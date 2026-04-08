package org.example.aicodehelper.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * RAG 配置类。
 * 负责加载本地知识库文档、完成切分和向量化，并暴露给大模型服务使用的检索器；
 * 如果初始化失败，会自动降级为空检索，避免影响应用启动。
 */
@Configuration
@Slf4j
public class RagConfig {

    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    public ContentRetriever contentRetriever() {
        try {
            List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
            DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(1000, 200);
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(paragraphSplitter)
                    .textSegmentTransformer(textSegment -> TextSegment.from(
                            textSegment.metadata().getString("file_name") + "\n" + textSegment.text(),
                            textSegment.metadata()
                    ))
                    .embeddingModel(qwenEmbeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            ingestor.ingest(documents);

            log.info("RAG initialized successfully with {} documents.", documents.size());
            return EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(qwenEmbeddingModel)
                    .maxResults(5)
                    .minScore(0.75)
                    .build();
        } catch (Exception ex) {
            log.warn("RAG initialization skipped. The application will start without knowledge-base retrieval. Cause: {}", ex.getMessage());
            return query -> Collections.<Content>emptyList();
        }
    }
}
