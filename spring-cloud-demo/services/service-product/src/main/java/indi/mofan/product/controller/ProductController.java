package indi.mofan.product.controller;


import indi.mofan.product.bean.Product;
import indi.mofan.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mofan
 * @date 2025/3/23 17:24
 */
@RestController
// @RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/product/{id}")
    @SneakyThrows
    public Product getProduct(@PathVariable("id") Long id,
                              HttpServletRequest request) {
        String header = request.getHeader("X-Token");
        System.out.println("Hello, XToken: " + header);
        Product product = productService.getProductById(id);

        /*
         * 模拟慢调用
         */
        // TimeUnit.SECONDS.sleep(2);

        /*
         * 模拟异常
         */
        // int i = 1 / 0;

        return product;
    }
}
