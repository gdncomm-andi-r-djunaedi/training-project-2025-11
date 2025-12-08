package MemberService.MemberService.controller;

import MemberService.MemberService.common.ApiResponse;
import MemberService.MemberService.common.ResponseUtil;
import MemberService.MemberService.dto.LoginDto;
import MemberService.MemberService.dto.RegisterDto;
import MemberService.MemberService.security.JWTUtil;
import MemberService.MemberService.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    MemberService memberService;

    @Autowired
    JWTUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody RegisterDto registerDto){
        return ResponseEntity.ok(ResponseUtil.success(memberService.register(registerDto)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginDto loginDto){
        String token=memberService.login(loginDto);
        return ResponseEntity.ok(ResponseUtil.success(token));
    }

    @GetMapping("/getProfile")
    public ResponseEntity<ApiResponse<?>> getProfile(@RequestHeader("X-User-Id") String userId){
       return ResponseEntity.ok(ResponseUtil.success(memberService.getProfile(userId)));
    }

    @PostMapping("/bulk-register")
    public ResponseEntity<ApiResponse<?>> bulkRegister(@RequestParam(defaultValue = "5000") int count){
        return ResponseEntity.ok(
                ResponseUtil.success(memberService.bulkRegister(count))
        );
    }

}
