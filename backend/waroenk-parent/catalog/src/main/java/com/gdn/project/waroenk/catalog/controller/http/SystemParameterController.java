package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.MultipleSystemParameterRequest;
import com.gdn.project.waroenk.catalog.MultipleSystemParameterResponse;
import com.gdn.project.waroenk.catalog.OneSystemParameterRequest;
import com.gdn.project.waroenk.catalog.SystemParameterData;
import com.gdn.project.waroenk.catalog.SystemParameterServiceGrpc;
import com.gdn.project.waroenk.catalog.constant.ApiConstant;
import com.gdn.project.waroenk.catalog.dto.BasicDto;
import com.gdn.project.waroenk.catalog.dto.systemparameter.ListOfSystemParameterResponseDto;
import com.gdn.project.waroenk.catalog.dto.systemparameter.SystemParameterResponseDto;
import com.gdn.project.waroenk.catalog.dto.systemparameter.UpsertSystemParameterRequestDto;
import com.gdn.project.waroenk.catalog.mapper.SystemParameterMapper;
import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.SortBy;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("catalogHttpSystemParameterController")
public class SystemParameterController {
  private static final SystemParameterMapper mapper = SystemParameterMapper.INSTANCE;
  private final SystemParameterServiceGrpc.SystemParameterServiceBlockingStub grpcClient;

  @Autowired
  public SystemParameterController(
      @GrpcClient("catalog-service") SystemParameterServiceGrpc.SystemParameterServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @GetMapping("/system-parameter")
  public SystemParameterResponseDto findOneSystemParameter(@RequestParam String variable) {
    SystemParameterData response =
        grpcClient.findOneSystemParameter(OneSystemParameterRequest.newBuilder().setVariable(variable).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/system-parameter/all")
  public ListOfSystemParameterResponseDto findAllSystemParameters(
      @RequestParam(required = false) String variable,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
    MultipleSystemParameterRequest.Builder builder = MultipleSystemParameterRequest.newBuilder().setSize(size);
    if (StringUtils.isNotBlank(variable)) builder.setVariable(variable);
    if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
    builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());
    
    MultipleSystemParameterResponse response = grpcClient.findAllSystemParameter(builder.build());
    return mapper.toResponseDto(response);
  }

  @DeleteMapping("/system-parameter")
  public BasicDto deleteOneSystemParameter(@RequestParam String variable) {
    Basic response =
        grpcClient.deleteSystemParameter(OneSystemParameterRequest.newBuilder().setVariable(variable).build());
    return mapper.toBasicDto(response);
  }

  @PostMapping("/system-parameter")
  public SystemParameterResponseDto upsertSystemParameter(
      @Valid @RequestBody UpsertSystemParameterRequestDto requestDto) {
    SystemParameterData response = grpcClient.upsertSystemParameter(mapper.toRequestGrpc(requestDto));
    return mapper.toResponseDto(response);
  }
}






