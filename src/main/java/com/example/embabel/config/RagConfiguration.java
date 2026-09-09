package com.example.embabel.config;

import com.embabel.agent.rag.ingestion.ContentChunker;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.agent.rag.tools.ToolishRag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG（检索增强生成）配置，组装 embabel 的 Lucene 全文检索引擎。
 *
 * <p>{@link LuceneSearchOperations} 提供基于内存的 Lucene 索引，
 * 支持 BM25 文本搜索（当前不使用向量/嵌入搜索，也未启用关键词提取）。
 * {@link ToolishRag} 将其包装为 LlmReference，将搜索工具暴露给 LLM 调用。
 *
 * <p>文档摄入由 {@link DocumentIngestionRunner} 在启动时通过
 * {@link LuceneSearchOperations#writeAndChunkDocument} 完成。
 */
@Configuration
public class RagConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RagConfiguration.class);

    /**
     * 索引名称
     */
    @Value("${insurance.rag.lucene.name:insurance-lucene}")
    private String luceneName;

    /**
     * 每个文档片段最多 1000 字符
     */
    @Value("${insurance.rag.lucene.chunk-size:1000}")
    private int chunkSize;

    /**
     * 相邻片段重叠 200 字符，避免语义在边界断裂
     * <p>
     *     下一片从上一片结尾往前 200 字符处开始
     * </p>
     */
    @Value("${insurance.rag.lucene.chunk-overlap:200}")
    private int chunkOverlap;

    /**
     * 搜索引擎
     *
     * @return 搜索引擎对象
     */
    @Bean
    public LuceneSearchOperations luceneSearchOperations() {
        logger.info("Creating LuceneSearchOperations (text-only, no embedding): name={}, chunkSize={}, chunkOverlap={}",
                luceneName, chunkSize, chunkOverlap);

        // 只有在 embeddingService != null（启用向量搜索），文档切片后产生 N 个 Chunk ->  不是一个个单独发给 Embedding API
        // -> 每 100 个 Chunk 打包成一批，批量请求 -> 减少 HTTP 调用次数，提升摄入速度
        var chunkerConfig = new ContentChunker.Config(chunkSize, chunkOverlap, 100);

        var ops = new LuceneSearchOperations(
                luceneName,
                /* enhancers */ java.util.List.of(),
                /* embeddingService */ null,
                /* keywordExtractor */ null,
                /* vectorWeight */ 0.0,
                /* chunkerConfig */ chunkerConfig,
                /* chunkTransformer */ com.embabel.agent.rag.ingestion.ChunkTransformer.NO_OP,
                /* indexPath */ null
        );

        logger.info("LuceneSearchOperations created: text-only mode");
        return ops;
    }

    @Bean
    public ToolishRag insuranceRag(LuceneSearchOperations luceneSearchOperations) {
        logger.info("Creating ToolishRag wrapping LuceneSearchOperations '{}'", luceneName);

        return new ToolishRag(
                /* name */        "insurance_docs",
                /* description */ """
                        Search insurance policy documents, claims guides, and FAQs about \
                        comprehensive vehicle insurance. Use textSearch for BM25 full-text \
                        search. Try multiple queries with different keywords if the first \
                        search doesn't yield good results.""",
                /* searchOperations */ luceneSearchOperations,
                /* goal */ """
                        Always search the knowledge base before answering user questions \
                        about insurance. Use Chinese keywords for Chinese questions and \
                        English keywords for English questions. If results are insufficient, \
                        try different query formulations or use broadenChunk to expand context."""
        );
    }
}
