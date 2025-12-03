package com.gdn.faurihakim.member.web.model;

import com.blibli.oss.backend.command.loom.executor.CommandExecutor;
import com.blibli.oss.backend.common.helper.ResponseHelper;
import com.blibli.oss.backend.common.model.response.Response;
import com.gdn.faurihakim.member.command.CreateMemberCommand;
import com.gdn.faurihakim.member.command.GetMemberCommand;
import com.gdn.faurihakim.member.command.UpdateMemberCommand;
import com.gdn.faurihakim.member.command.model.CreateMemberCommandRequest;
import com.gdn.faurihakim.member.command.model.GetMemberCommandRequest;
import com.gdn.faurihakim.member.command.model.UpdateMemberCommandRequest;
import com.gdn.faurihakim.member.web.model.request.CreateMemberWebRequest;
import com.gdn.faurihakim.member.web.model.request.UpdateMemberWebRequest;
import com.gdn.faurihakim.member.web.model.response.CreateMemberWebResponse;
import com.gdn.faurihakim.member.web.model.response.GetMemberWebResponse;
import com.gdn.faurihakim.member.web.model.response.UpdateMemberWebResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(value = "/api")
public class MemberController {

    @Autowired
    private CommandExecutor executor;

    @Operation(summary = "Get member")
    @GetMapping(
            value = "/members",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<GetMemberWebResponse> getMember(String email) {
        log.info("Receive get member API");

        GetMemberWebResponse response = executor.execute(
                GetMemberCommand.class, GetMemberCommandRequest.builder()
                        .email(email)
                        .build());

        return ResponseHelper.ok(response);
    }

    @Operation(summary = "Create member")
    @PostMapping(
            value = "/members",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<CreateMemberWebResponse> createMember(
            @RequestBody CreateMemberWebRequest requestBody) {
        log.info("Receive create member API");

        CreateMemberCommandRequest commandRequest = new CreateMemberCommandRequest();
        BeanUtils.copyProperties(requestBody, commandRequest);
        CreateMemberWebResponse response = executor.execute(CreateMemberCommand.class, commandRequest);
        return ResponseHelper.ok(response);
    }

    @Operation(summary = "Update member")
    @PutMapping(
            value = "/members/{memberId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<UpdateMemberWebResponse> updateMember(
            @PathVariable String memberId,
            @RequestBody UpdateMemberWebRequest requestBody) {
        log.info("Receive update member API");
        UpdateMemberCommandRequest commandRequest = new UpdateMemberCommandRequest();
        BeanUtils.copyProperties(requestBody, commandRequest);
        commandRequest.setMemberId(memberId);
        UpdateMemberWebResponse response = executor.execute(UpdateMemberCommand.class, commandRequest);
        return ResponseHelper.ok(response);
    }
}
