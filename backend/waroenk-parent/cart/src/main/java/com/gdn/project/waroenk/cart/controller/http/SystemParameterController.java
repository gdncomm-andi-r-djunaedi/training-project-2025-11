package com.gdn.project.waroenk.cart.controller.http;

import com.gdn.project.waroenk.cart.*;
import com.gdn.project.waroenk.cart.constant.ApiConstant;
import com.gdn.project.waroenk.cart.dto.BasicDto;
import com.gdn.project.waroenk.cart.dto.systemparameter.*;
import com.gdn.project.waroenk.cart.mapper.SystemParameterMapper;
import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.SortBy;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("systemParameterHttpController")
public class SystemParameterController {

    private static final SystemParameterMapper mapper = SystemParameterMapper.INSTANCE;
    private final CartSystemParameterServiceGrpc.CartSystemParameterServiceBlockingStub grpcClient;

    @Autowired
    public SystemParameterController(
            @GrpcClient("cart-service") CartSystemParameterServiceGrpc.CartSystemParameterServiceBlockingStub grpcClient) {
        this.grpcClient = grpcClient;
    }

    @PostMapping("/system-parameters")
    public SystemParameterResponseDto createSystemParameter(@Valid @RequestBody UpsertSystemParameterRequestDto requestDto) {
        SystemParameterData response = grpcClient.createSystemParameter(mapper.toUpsertRequestGrpc(requestDto));
        return mapper.toResponseDto(response);
    }

    @PutMapping("/system-parameters")
    public SystemParameterResponseDto updateSystemParameter(@Valid @RequestBody UpsertSystemParameterRequestDto requestDto) {
        SystemParameterData response = grpcClient.updateSystemParameter(mapper.toUpsertRequestGrpc(requestDto));
        return mapper.toResponseDto(response);
    }

    @DeleteMapping("/system-parameters/{variable}")
    public BasicDto deleteSystemParameter(@PathVariable String variable) {
        Basic response = grpcClient.deleteSystemParameter(mapper.toOneRequestGrpc(variable));
        return mapper.toBasicDto(response);
    }

    @GetMapping("/system-parameters/{variable}")
    public SystemParameterResponseDto findSystemParameterByVariable(@PathVariable String variable) {
        SystemParameterData response = grpcClient.findSystemParameterByVariable(mapper.toOneRequestGrpc(variable));
        return mapper.toResponseDto(response);
    }

    @GetMapping("/system-parameters")
    public ListOfSystemParameterResponseDto filterSystemParameters(
            @RequestParam(required = false) String variable,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        MultipleSystemParameterRequest.Builder builder = MultipleSystemParameterRequest.newBuilder().setSize(size);
        if (StringUtils.isNotBlank(variable)) builder.setVariable(variable);
        if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
        builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());

        MultipleSystemParameterResponse response = grpcClient.filterSystemParameters(builder.build());
        return mapper.toResponseDto(response);
    }
}




