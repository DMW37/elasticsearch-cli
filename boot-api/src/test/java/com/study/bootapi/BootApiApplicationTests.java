package com.study.bootapi;

import com.study.bootapi.dao.GoodsRepository;
import com.study.bootapi.domain.Goods;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
class BootApiApplicationTests {

    @Resource
    private GoodsRepository goodsRepository;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 批量插入，查询所有
     */
    @Test
    public void testSaveAll() {
        List<Goods> list = new ArrayList<>();
        list.add(new Goods(152, "测试手机1", new BigDecimal("500"), "jpg"));
        list.add(new Goods(153, "测试手机2", new BigDecimal("800"), "png"));
        goodsRepository.saveAll(list);
        // 查询所有
        Iterable<Goods> all = goodsRepository.findAll();
        all.forEach(System.out::println);
    }

    @Test
    public void testFindByName() {
        goodsRepository.findByGoodsName("中国").forEach(System.out::println);
    }

    @Test
    public void testFindById() {
        System.out.println(goodsRepository.findByIdValue(100));
    }

    @Test
    public void test() {
        System.out.println(goodsRepository.findById(10));
    }

    /**
     * 索引操作
     */
    @Test
    public void testIndex() {
        // 设置索引信息（实体类），返回indexOperations
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(Goods.class);
        indexOperations.create();
        // 创建索引映射
        Document mapping = indexOperations.createMapping();
        // 将映射写入索引
        indexOperations.putMapping(mapping);
        // 获取索引
        Map<String, Object> map = indexOperations.getMapping();
        map.forEach((k, v) -> System.out.println(k + "-->" + v));
        // 索引是否存在
        System.out.println(indexOperations.exists());

        // 删除索引
        indexOperations.delete();
    }

    /**
     * 增删改
     */
    @Test
    public void testDocumentCUD() {
        /**
         * 根据id和索引删除，返回删除的id
         * 第一个参数：id，String类型
         * 第二个参数：索引库对象
         */
//         String count = elasticsearchRestTemplate.delete("110", IndexCoordinates.of("shop"));
//         System.out.println(count);
        /**
         * 删除查询结果
         * 第一个参数：查询对象
         * 第二个参数：索引类字节码
         * 第三个参数：索引库对象
         */
//        elasticsearchRestTemplate.delete(new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.matchQuery("goodsName", "测试"))
//                .build());
        //新增/更新（id不存在就新增，存在就更新）
        List<Goods> list = new ArrayList<>();
        list.add(new Goods(150, "测试手机3", new BigDecimal("100"), "jpg"));
        list.add(new Goods(151, "测试手机4", new BigDecimal("200"), "png"));
        Iterable<Goods> save = elasticsearchRestTemplate.save(list);
        save.forEach(System.out::println);


    }

    /**
     * 匹配查询
     */
    @Test
    public void testMatch() {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        NativeSearchQuery query = nativeSearchQueryBuilder
                /**
                 * 第一个参数：关键词
                 * 第二个参数：对应es的字段
                 */
                .withQuery(QueryBuilders.multiMatchQuery("中国移动联通电信", "goodsName"))
                .build();
        SearchHits<Goods> search = elasticsearchRestTemplate.search(query, Goods.class);
        search.forEach(searchHit -> System.out.println(searchHit.getContent()));
    }

    /**
     * 分页，排序,高亮查询
     */
    @Test
    public void testPage() {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        NativeSearchQuery query = nativeSearchQueryBuilder
                .withQuery(QueryBuilders.multiMatchQuery("中国移动联通电信", "goodsName"))
                .withPageable(PageRequest.of(0, 5, Sort.Direction.DESC, "goodsId", "marketPrice"))
                .withHighlightFields(new HighlightBuilder.Field("goodsName")
                        .preTags("<span style='color:red;'>")
                        .postTags("</span>"))
                .build();

        SearchHits<Goods> search = elasticsearchRestTemplate.search(query, Goods.class);
        for (SearchHit<Goods> searchHit : search) {
            System.out.println(searchHit.getId());
            //分数
            System.out.println(searchHit.getScore());
            //排序的值
            Integer sortValues = (Integer) searchHit.getSortValues().get(0);
            System.out.println(sortValues);
            //高亮信息
            String highlightMessage = searchHit.getHighlightField("goodsName").get(0);
            System.out.println(highlightMessage);
            //结果对象
            System.out.println(searchHit.getContent());
        }
    }
}
