package com.blibli.ecommerceProject.services;

import com.blibli.ecommerceProject.dto.MemberProfiledto;
import com.blibli.ecommerceProject.dto.MemberValidationRequestdto;
import com.blibli.ecommerceProject.dto.Memberdto;
import org.springframework.http.ResponseEntity;

public interface MemberService{
    Memberdto registerMember(Memberdto memberdto);

    boolean validateCredentials(MemberValidationRequestdto memberValidationRequestdto);

    MemberProfiledto getUserProfile(String authToken);
}
