package com.training.marketplace.member.filter;

import com.training.marketplace.member.utils.JwtUtils;
import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

@RequiredArgsConstructor
public class JwtServerInterceptor implements ServerInterceptor, Ordered {

    @Autowired
    private final JwtUtils jwtUtils;

    @Autowired
    private final UserDetails userDetails;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {

        final String authHeader = metadata.get(Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER));

        Status status = Status.OK;

        if (authHeader == null) {
            status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing");
        } else if (!authHeader.startsWith("Bearer ")) {
            status = Status.UNAUTHENTICATED.withDescription("Unknown authorization type");
        } else {
            String token = authHeader.substring(7).trim();
            String username = jwtUtils.extractUsernameFromToken(token);
            if (username != null) {
                if (jwtUtils.isValidToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    Context context = Context.current().withValue(Context.key("clientId"), username);
                    return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
                }
            }
        }

        serverCall.close(status, new Metadata());
        return new ServerCall.Listener<ReqT>() {
            // noop
        };
    }

    @Override
    public int getOrder() {
        return -11;
    }
}
