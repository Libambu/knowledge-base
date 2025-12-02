package indi.mofan.order;


import indi.mofan.order.feign.MockUrlFeignClient;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author mofan
 * @date 2025/4/7 22:31
 */
@SpringBootTest
public class OpenFeignTest {
    @Autowired
    private MockUrlFeignClient mockUrlFeignClient;

    @Test
    public void testUrl() {
        String value = mockUrlFeignClient.getProduct(1L, 2L);
        // language=JSON
        String expectValue = """
                {
                  "id": 2,
                  "totalAmount": 198,
                  "userId": 1,
                  "nickname": "mofan",
                  "address": "Chengdu",
                  "productList": [
                    {
                      "id": 2,
                      "price": 99,
                      "productName": "apple-2",
                      "num": 2
                    }
                  ]
                }
                """;
        JsonAssertions.assertThatJson(value).isEqualTo(expectValue);
    }
}
