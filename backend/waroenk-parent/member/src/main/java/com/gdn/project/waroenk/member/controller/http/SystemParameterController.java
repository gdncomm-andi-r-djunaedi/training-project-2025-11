package com.gdn.project.waroenk.member.controller.http;

import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.SortBy;
import com.gdn.project.waroenk.member.MultipleSystemParameterRequest;
import com.gdn.project.waroenk.member.MultipleSystemParameterResponse;
import com.gdn.project.waroenk.member.OneSystemParameterRequest;
import com.gdn.project.waroenk.member.SystemParameterData;
import com.gdn.project.waroenk.member.SystemParameterServiceGrpc;
import com.gdn.project.waroenk.member.constant.ApiConstant;
import com.gdn.project.waroenk.member.constant.Sort;
import com.gdn.project.waroenk.member.dto.BasicDto;
import com.gdn.project.waroenk.member.dto.ListOfSystemParameterResponseDto;
import com.gdn.project.waroenk.member.dto.SystemParameterResponseDto;
import com.gdn.project.waroenk.member.dto.UpsertSystemParameterRequestDto;
import com.gdn.project.waroenk.member.mapper.SystemParameterMapper;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("memberHttpSystemParameterController")
public class SystemParameterController {
  private static final SystemParameterMapper mapper = SystemParameterMapper.INSTANCE;
  private final SystemParameterServiceGrpc.SystemParameterServiceBlockingStub grpcClient;

  @Autowired
  public SystemParameterController(@GrpcClient(
      "member-service") SystemParameterServiceGrpc.SystemParameterServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @GetMapping("/system-parameter")
  public SystemParameterResponseDto findOneSystemParameter(@RequestParam String variable) {
    SystemParameterData response =
        grpcClient.findOneSystemParameter(OneSystemParameterRequest.newBuilder().setVariable(variable).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/system-parameter/all")
  public ListOfSystemParameterResponseDto findAllSystemParameters(@RequestParam(required = false) String variable,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") Sort sortDirection) {
    MultipleSystemParameterRequest.Builder requestBuilder = MultipleSystemParameterRequest.newBuilder();
    requestBuilder.setSize(size);
    
    // Only set optional fields if they have values (protobuf doesn't allow null)
    if (StringUtils.isNotBlank(variable)) {
      requestBuilder.setVariable(variable);
    }
    if (StringUtils.isNotBlank(cursor)) {
      requestBuilder.setCursor(cursor);
    }
    if (StringUtils.isNotBlank(sortBy)) {
      SortBy.Builder sortBuilder = SortBy.newBuilder();
      sortBuilder.setField(sortBy);
      if(ObjectUtils.isNotEmpty(sortDirection)){
        sortBuilder.setDirection(sortDirection.getShortName()).build();
      }
      requestBuilder.setSortBy(sortBuilder.build());
    }
    
    MultipleSystemParameterResponse response = grpcClient.findAllSystemParameter(requestBuilder.build());
    return mapper.toResponseDto(response);
  }

  @DeleteMapping("/system-parameter")
  public BasicDto deleteOneSystemParameter(@RequestParam String variable) {
    Basic response =
        grpcClient.deleteSystemParameter(OneSystemParameterRequest.newBuilder().setVariable(variable).build());
    return mapper.toBasicDto(response);
  }

  @PostMapping("/system-parameter")
  public SystemParameterResponseDto upsertSystemParameter(@Valid @RequestBody UpsertSystemParameterRequestDto requestDto) {
    SystemParameterData response = grpcClient.upsertSystemParameter(mapper.toRequestGrpc(requestDto));
    return mapper.toResponseDto(response);
  }
}
