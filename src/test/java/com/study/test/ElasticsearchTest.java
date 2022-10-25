package com.study.test;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: 邓明维
 * @date: 2022/10/25
 * @description:
 */
public class ElasticsearchTest {
    // ES服务器IP
    private final static String HOST = "localhost";
    // ES服务器连接方式
    private final static String SCHEME = "http";
    // 初始化ES集群  参数分别为：IP，端口，连接方式(默认为http)
    private final static HttpHost[] HTTP_HOSTS = {
            new HttpHost(HOST, 9201, SCHEME),
            new HttpHost(HOST, 9202, SCHEME),
            new HttpHost(HOST, 9203, SCHEME),
            new HttpHost(HOST, 9204, SCHEME),
            new HttpHost(HOST, 9205, SCHEME),
            new HttpHost(HOST, 9206, SCHEME)
    };

    // 客户端
    private RestHighLevelClient client = null;

    @Before
    public void beforeConnection() {
        client = new RestHighLevelClient(RestClient.builder(HTTP_HOSTS));
    }

    @After
    public void close() {
        if (client != null)
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * 创建索引
     *
     * @throws IOException
     */
    @Test
    public void testCreate() throws IOException {
        // 添加数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("username", "zhangsan");
        jsonMap.put("age", 18);
        jsonMap.put("address", "sh");
        // 指定索引库id和数据
        IndexRequest indexRequest = new IndexRequest("ik").id("5").source(jsonMap);
        // 执行请求
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
    }

    /**
     * 修改数据
     *
     * @throws IOException
     */
    @Test
    public void testUpdate() throws IOException {
        // 准备数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("username", "lisi");
        jsonMap.put("age", 20);
        jsonMap.put("address", "bj");
        // 指定索引库id和数据
        UpdateRequest updateRequest = new UpdateRequest("ik", "5").doc(jsonMap);
        // 执行请求
        UpdateResponse update = client.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(update.toString());
    }

    /**
     * 删除元素
     *
     * @throws IOException
     */
    @Test
    public void testDelete() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("ik", "5");
        DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(deleteRequest);
    }

    /**
     * 批量增删改
     */
    @Test
    public void testCUD() throws IOException {
        // 初始化BulkRequest【bulk--大批】
        BulkRequest request = new BulkRequest();
        // 创建索引库和id以及数据
        // 批量添加
        request.add(new IndexRequest("ik").id("8").source(XContentType.JSON, "username", "zhangsan", "age", "18"));
        request.add(new IndexRequest("ik").id("9")
                .source(XContentType.JSON, "username", "lisi", "age", 20));

        // 批量修改
        request.add(new UpdateRequest("ik", "6").doc(XContentType.JSON, "username", "9527"));

        // 批量删除
        request.add(new DeleteRequest("ik", "6"));

        // 执行
        BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(bulkResponse);
    }

    /**
     * 批量查询-查询所有
     */
    @Test
    public void testBilkQueryAll() throws IOException {
        // 批量查询
        // 1.指定索引库
        // 可以指定多个库
        SearchRequest searchRequest = new SearchRequest("ik", "shop");

        // 2.构建查询对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 3.添加查询条件
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        // 4.执行请求
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(searchResponse);

        // 总条数
        System.out.println(searchResponse.getHits().getTotalHits().value);

        // 结果数据(如果不设置返回条数，大于十条默认只返回十条)
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println("分数:" + hit.getScore());
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println("索引:" + hit.getIndex());
            System.out.println("id:" + hit.getId());
            for (Map.Entry<String, Object> stringObjectEntry : sourceAsMap.entrySet()) {
                System.out.println(stringObjectEntry.getKey() + " : " + stringObjectEntry.getValue());
            }
            System.out.println("---------------");
        }
    }

    /**
     * 批量查询-匹配查询
     */
    @Test
    public void testMatchQuery() throws IOException {
        // 匹配查询
        // 1.指定索引库
        SearchRequest searchRequest = new SearchRequest("shop");
        // 2.构建查询对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 3.添加查询条件  从content-goodsName 字段中查询
        String key = "中国";
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(key, "content", "goodsName"));

        // 4.执行请求
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 总条数
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println("分数：" + hit.getScore());
            Map<String, Object> source = hit.getSourceAsMap();
            System.out.println("index -> " + hit.getIndex());
            System.out.println("id -> " + hit.getId());
            for (Map.Entry<String, Object> s : source.entrySet()) {
                System.out.println(s.getKey() + " -- " + s.getValue());
            }
            System.out.println("----------------------------");
        }
    }

    /**
     * 批量查询-分页查询-按分数或id排序
     */
    @Test
    public void test() throws IOException {
        // 分页查询
        SearchRequest searchRequest = new SearchRequest("shop");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 添加分页条件，从第 0 个开始，返回 5 个
        searchSourceBuilder.from(0).size(5);

        String key = "中国移动联通电信";
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(key, "goodsName"));
        // 按照 score 正序排列(默认倒序)
        searchSourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.ASC));
        // 并且按照 id 倒序排列(分数字段会失效返回 NaN)
        searchSourceBuilder.sort(SortBuilders.fieldSort("_id").order(SortOrder.DESC));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 总条数
        System.out.println(searchResponse.getHits().getTotalHits().value);
        // 结果数据(如果不设置返回条数，大于十条默认只返回十条)
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println("分数：" + hit.getScore());
            Map<String, Object> source = hit.getSourceAsMap();
            System.out.println("index -> " + hit.getIndex());
            System.out.println("id -> " + hit.getId());
            for (Map.Entry<String, Object> s : source.entrySet()) {
                System.out.println(s.getKey() + " -- " + s.getValue());
            }
            System.out.println("----------------------------");
        }
    }

    /**
     * 批量查询-分页查询-高亮查询
     */
    @Test
    public void testHighLight() throws IOException {
        SearchRequest searchRequest = new SearchRequest("shop");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0).size(5).sort(SortBuilders.scoreSort().order(SortOrder.DESC));
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 指定高亮字段和高亮样式
        highlightBuilder.field("goodsName")
                .preTags("<span style='color:red;'>")
                .postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        // 指定从 goodsName 字段中查询
        String key = "中国移动联通电信";
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(key, "goodsName"));
        // 执行请求
        searchRequest.source(searchSourceBuilder);
        SearchResponse response =client.search(searchRequest,RequestOptions.DEFAULT);

        // 总条数
        System.out.println(response.getHits().getTotalHits().value);
        // 结果数据(如果不设置返回条数，大于十条默认只返回十条)
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            // 构建项目中所需的数据结果集
            String highlightMessage = String.valueOf(hit.getHighlightFields().get("goodsName").fragments()[0]);
            Integer goodsId = Integer.valueOf((Integer) hit.getSourceAsMap().get("goodsId"));
            String goodsName = String.valueOf(hit.getSourceAsMap().get("goodsName"));
            BigDecimal marketPrice = new BigDecimal(String.valueOf(hit.getSourceAsMap().get("marketPrice")));
            String originalImg = String.valueOf(hit.getSourceAsMap().get("originalImg"));
            System.out.println("goodsId -> " + goodsId);
            System.out.println("goodsName -> " + goodsName);
            System.out.println("highlightMessage -> " + highlightMessage);
            System.out.println("marketPrice -> " + marketPrice);
            System.out.println("originalImg -> " + originalImg);
            System.out.println("----------------------------");
        }
    }
}
