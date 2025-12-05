package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.constant.ApiConstant;
import com.gdn.project.waroenk.catalog.dto.VersionResponseDto;
import com.gdn.project.waroenk.catalog.mapper.VersionMapper;
import com.gdn.project.waroenk.common.Empty;
import com.gdn.project.waroenk.common.VersionResponse;
import com.gdn.project.waroenk.common.VersionServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("catalogHttpVersionController")
public class VersionController {
  private static final VersionMapper mapper = VersionMapper.INSTANCE;
  private final VersionServiceGrpc.VersionServiceBlockingStub grpcClient;

  @Autowired
  public VersionController(@GrpcClient("catalog-service") VersionServiceGrpc.VersionServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @GetMapping("/version")
  public VersionResponseDto getVersion() {
    VersionResponse response = grpcClient.getVersion(Empty.getDefaultInstance());
    return mapper.toResponseDto(response);
  }
}






