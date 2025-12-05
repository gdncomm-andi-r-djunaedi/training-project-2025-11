package com.gdn.project.waroenk.member.controller.http;

import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.CacheServiceGrpc;
import com.gdn.project.waroenk.common.Empty;
import com.gdn.project.waroenk.common.FlushPattern;
import com.gdn.project.waroenk.member.constant.ApiConstant;
import com.gdn.project.waroenk.member.dto.BasicDto;
import com.gdn.project.waroenk.member.mapper.GenericMapper;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("memberHttpCacheController")
public class CacheController {
  private static final GenericMapper mapper = GenericMapper.INSTANCE;
  private final CacheServiceGrpc.CacheServiceBlockingStub grpcClient;

  @Autowired
  public CacheController(@GrpcClient("member-service") CacheServiceGrpc.CacheServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @DeleteMapping("/flush-all")
  public BasicDto flushAllCache() {
    Basic response = grpcClient.flushAll(Empty.getDefaultInstance());
    return mapper.toBasicDto(response);
  }

  @DeleteMapping("/flush")
  public BasicDto flushCache(@RequestParam String key) {
    Basic response = grpcClient.flush(FlushPattern.newBuilder().setKey(key).build());
    return mapper.toBasicDto(response);
  }
}
