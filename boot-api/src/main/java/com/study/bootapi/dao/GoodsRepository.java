package com.study.bootapi.dao;

import com.study.bootapi.domain.Goods;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @author: 邓明维
 * @date: 2022/10/25
 * @description:
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Integer> {
    /**
     * 根据商品名查询
     * @param goodsName
     * @return
     */
    List<Goods> findByGoodsName(String goodsName);

    /**
     * 根据id查询商品
     * ？0为占位符
     * @param id
     * @return
     */
    @Query("{\"match\":{\"goodsId\":{\"query\":\"?0\"}}}")
    Goods findByIdValue(Integer id);
}
