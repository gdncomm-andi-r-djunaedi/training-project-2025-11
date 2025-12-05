package com.gdn.project.waroenk.cart.controller.http;

import com.gdn.project.waroenk.cart.constant.ApiConstant;
import com.gdn.project.waroenk.cart.dto.BasicDto;
import com.gdn.project.waroenk.cart.mapper.GenericMapper;
import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.CacheServiceGrpc;
import com.gdn.project.waroenk.common.Empty;
import com.gdn.project.waroenk.common.FlushPattern;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("cacheHttpController")
public class CacheController {

    private static final GenericMapper mapper = GenericMapper.INSTANCE;
    private final CacheServiceGrpc.CacheServiceBlockingStub grpcClient;

    @Autowired
    public CacheController(
            @GrpcClient("cart-service") CacheServiceGrpc.CacheServiceBlockingStub grpcClient) {
        this.grpcClient = grpcClient;
    }

    @DeleteMapping("/cache/flush-all")
    public BasicDto flushAll() {
        Basic response = grpcClient.flushAll(Empty.newBuilder().build());
        return mapper.toBasicDto(response);
    }

    @DeleteMapping("/cache/flush")
    public BasicDto flush(@RequestParam String pattern) {
        Basic response = grpcClient.flush(FlushPattern.newBuilder().setKey(pattern).build());
        return mapper.toBasicDto(response);
    }
}




