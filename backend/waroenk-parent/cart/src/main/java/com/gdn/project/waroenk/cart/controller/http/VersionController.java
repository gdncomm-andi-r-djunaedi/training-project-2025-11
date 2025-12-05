package com.gdn.project.waroenk.cart.controller.http;

import com.gdn.project.waroenk.cart.constant.ApiConstant;
import com.gdn.project.waroenk.cart.dto.VersionResponseDto;
import com.gdn.project.waroenk.common.Empty;
import com.gdn.project.waroenk.common.VersionResponse;
import com.gdn.project.waroenk.common.VersionServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("versionHttpController")
public class VersionController {

    private final VersionServiceGrpc.VersionServiceBlockingStub grpcClient;

    @Autowired
    public VersionController(
            @GrpcClient("cart-service") VersionServiceGrpc.VersionServiceBlockingStub grpcClient) {
        this.grpcClient = grpcClient;
    }

    @GetMapping("/version")
    public VersionResponseDto getVersion() {
        VersionResponse response = grpcClient.getVersion(Empty.newBuilder().build());
        return new VersionResponseDto(response.getName(), response.getVersion());
    }
}




