package indi.mofan.order.expcetion;


import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.mofan.common.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

/**
 * @author mofan
 * @date 2025/4/23 22:20
 */
@Component
public class MyBlockExceptionHandler implements BlockExceptionHandler {

    private final ObjectMapper objectMapper;

    public MyBlockExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       String resourceName,
                       BlockException e) throws Exception {
        // too many request
        response.setStatus(429);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();

        R error = R.error(500, resourceName + " 被 Sentinel 限制了, 原因: " + e.getClass());

        String json = objectMapper.writeValueAsString(error);
        writer.write(json);

        writer.flush();
        writer.close();
    }
}
