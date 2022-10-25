package com.study.bootapi.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author zhoubin
 * @since 1.0.0
 */
@Document(indexName = "shop",shards = 5,replicas = 1,createIndex = false)
public class Goods implements Serializable {
    /**
     * 商品id
     */
    @Id
    private Integer goodsId;

    /**
     * 商品名称
     */
    @Field(type = FieldType.Text,analyzer = "ik_max_word")
    private String goodsName;

    /**
     * 市场价
     */
    @Field(type = FieldType.Double)
    private BigDecimal marketPrice;

    /**
     * 商品上传原始图
     */
    @Field(type = FieldType.Keyword)
    private String originalImg;

    /**
     * t_goods
     */
    private static final long serialVersionUID = 1L;

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName == null ? null : goodsName.trim();
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }

    public String getOriginalImg() {
        return originalImg;
    }

    public void setOriginalImg(String originalImg) {
        this.originalImg = originalImg == null ? null : originalImg.trim();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", goodsId=").append(goodsId);
        sb.append(", goodsName=").append(goodsName);
        sb.append(", marketPrice=").append(marketPrice);
        sb.append(", originalImg=").append(originalImg);
        sb.append("]");
        return sb.toString();
    }


	public Goods() {
	}

	public Goods(Integer goodsId, String goodsName, BigDecimal marketPrice, String originalImg) {
		this.goodsId = goodsId;
		this.goodsName = goodsName;
		this.marketPrice = marketPrice;
		this.originalImg = originalImg;
	}
}
