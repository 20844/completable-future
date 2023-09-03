package com.py.advanced._04_compare_price;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceResult {
    private int price;	// 平台价格
    private int discount;	// 折扣
    private int realPrice;// 最终价
    private String platform;// 商品平台

    public PriceResult(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "PriceResult{" +
                "平台='" + platform +
                ", 平台价=" + price +
                ", 优惠价=" + discount +
                ", 最终价=" + realPrice +
                '}';
    }
}
