package com.gdn.training.member.infrastructure.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Services
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.59.0)",
    comments = "Source: member_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class MemberServiceGrpc {

  private MemberServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "member.MemberService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest,
      com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse> getRegisterMemberMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RegisterMember",
      requestType = com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest.class,
      responseType = com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest,
      com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse> getRegisterMemberMethod() {
    io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest, com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse> getRegisterMemberMethod;
    if ((getRegisterMemberMethod = MemberServiceGrpc.getRegisterMemberMethod) == null) {
      synchronized (MemberServiceGrpc.class) {
        if ((getRegisterMemberMethod = MemberServiceGrpc.getRegisterMemberMethod) == null) {
          MemberServiceGrpc.getRegisterMemberMethod = getRegisterMemberMethod =
              io.grpc.MethodDescriptor.<com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest, com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RegisterMember"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MemberServiceMethodDescriptorSupplier("RegisterMember"))
              .build();
        }
      }
    }
    return getRegisterMemberMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest,
      com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse> getLoginMemberMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "LoginMember",
      requestType = com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest.class,
      responseType = com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest,
      com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse> getLoginMemberMethod() {
    io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest, com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse> getLoginMemberMethod;
    if ((getLoginMemberMethod = MemberServiceGrpc.getLoginMemberMethod) == null) {
      synchronized (MemberServiceGrpc.class) {
        if ((getLoginMemberMethod = MemberServiceGrpc.getLoginMemberMethod) == null) {
          MemberServiceGrpc.getLoginMemberMethod = getLoginMemberMethod =
              io.grpc.MethodDescriptor.<com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest, com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "LoginMember"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MemberServiceMethodDescriptorSupplier("LoginMember"))
              .build();
        }
      }
    }
    return getLoginMemberMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.google.protobuf.Empty> getLogoutMemberMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "LogoutMember",
      requestType = com.google.protobuf.Empty.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      com.google.protobuf.Empty> getLogoutMemberMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, com.google.protobuf.Empty> getLogoutMemberMethod;
    if ((getLogoutMemberMethod = MemberServiceGrpc.getLogoutMemberMethod) == null) {
      synchronized (MemberServiceGrpc.class) {
        if ((getLogoutMemberMethod = MemberServiceGrpc.getLogoutMemberMethod) == null) {
          MemberServiceGrpc.getLogoutMemberMethod = getLogoutMemberMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "LogoutMember"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new MemberServiceMethodDescriptorSupplier("LogoutMember"))
              .build();
        }
      }
    }
    return getLogoutMemberMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest,
      com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse> getGetMemberProfileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetMemberProfile",
      requestType = com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest.class,
      responseType = com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest,
      com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse> getGetMemberProfileMethod() {
    io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest, com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse> getGetMemberProfileMethod;
    if ((getGetMemberProfileMethod = MemberServiceGrpc.getGetMemberProfileMethod) == null) {
      synchronized (MemberServiceGrpc.class) {
        if ((getGetMemberProfileMethod = MemberServiceGrpc.getGetMemberProfileMethod) == null) {
          MemberServiceGrpc.getGetMemberProfileMethod = getGetMemberProfileMethod =
              io.grpc.MethodDescriptor.<com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest, com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetMemberProfile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MemberServiceMethodDescriptorSupplier("GetMemberProfile"))
              .build();
        }
      }
    }
    return getGetMemberProfileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest,
      com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse> getValidateMemberMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ValidateMember",
      requestType = com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest.class,
      responseType = com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest,
      com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse> getValidateMemberMethod() {
    io.grpc.MethodDescriptor<com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest, com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse> getValidateMemberMethod;
    if ((getValidateMemberMethod = MemberServiceGrpc.getValidateMemberMethod) == null) {
      synchronized (MemberServiceGrpc.class) {
        if ((getValidateMemberMethod = MemberServiceGrpc.getValidateMemberMethod) == null) {
          MemberServiceGrpc.getValidateMemberMethod = getValidateMemberMethod =
              io.grpc.MethodDescriptor.<com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest, com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ValidateMember"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MemberServiceMethodDescriptorSupplier("ValidateMember"))
              .build();
        }
      }
    }
    return getValidateMemberMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MemberServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MemberServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MemberServiceStub>() {
        @java.lang.Override
        public MemberServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MemberServiceStub(channel, callOptions);
        }
      };
    return MemberServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MemberServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MemberServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MemberServiceBlockingStub>() {
        @java.lang.Override
        public MemberServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MemberServiceBlockingStub(channel, callOptions);
        }
      };
    return MemberServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MemberServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MemberServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MemberServiceFutureStub>() {
        @java.lang.Override
        public MemberServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MemberServiceFutureStub(channel, callOptions);
        }
      };
    return MemberServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Services
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void registerMember(com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest request,
        io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRegisterMemberMethod(), responseObserver);
    }

    /**
     */
    default void loginMember(com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest request,
        io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getLoginMemberMethod(), responseObserver);
    }

    /**
     */
    default void logoutMember(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getLogoutMemberMethod(), responseObserver);
    }

    /**
     */
    default void getMemberProfile(com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest request,
        io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetMemberProfileMethod(), responseObserver);
    }

    /**
     */
    default void validateMember(com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest request,
        io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getValidateMemberMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service MemberService.
   * <pre>
   * Services
   * </pre>
   */
  public static abstract class MemberServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return MemberServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MemberService.
   * <pre>
   * Services
   * </pre>
   */
  public static final class MemberServiceStub
      extends io.grpc.stub.AbstractAsyncStub<MemberServiceStub> {
    private MemberServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MemberServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MemberServiceStub(channel, callOptions);
    }

    /**
     */
    public void registerMember(com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest request,
        io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRegisterMemberMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void loginMember(com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest request,
        io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getLoginMemberMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void logoutMember(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getLogoutMemberMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getMemberProfile(com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest request,
        io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetMemberProfileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void validateMember(com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest request,
        io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getValidateMemberMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MemberService.
   * <pre>
   * Services
   * </pre>
   */
  public static final class MemberServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MemberServiceBlockingStub> {
    private MemberServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MemberServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MemberServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse registerMember(com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRegisterMemberMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse loginMember(com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getLoginMemberMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty logoutMember(com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getLogoutMemberMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse getMemberProfile(com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetMemberProfileMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse validateMember(com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getValidateMemberMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MemberService.
   * <pre>
   * Services
   * </pre>
   */
  public static final class MemberServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<MemberServiceFutureStub> {
    private MemberServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MemberServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MemberServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse> registerMember(
        com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRegisterMemberMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse> loginMember(
        com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getLoginMemberMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> logoutMember(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getLogoutMemberMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse> getMemberProfile(
        com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetMemberProfileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse> validateMember(
        com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getValidateMemberMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REGISTER_MEMBER = 0;
  private static final int METHODID_LOGIN_MEMBER = 1;
  private static final int METHODID_LOGOUT_MEMBER = 2;
  private static final int METHODID_GET_MEMBER_PROFILE = 3;
  private static final int METHODID_VALIDATE_MEMBER = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REGISTER_MEMBER:
          serviceImpl.registerMember((com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest) request,
              (io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse>) responseObserver);
          break;
        case METHODID_LOGIN_MEMBER:
          serviceImpl.loginMember((com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest) request,
              (io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse>) responseObserver);
          break;
        case METHODID_LOGOUT_MEMBER:
          serviceImpl.logoutMember((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_GET_MEMBER_PROFILE:
          serviceImpl.getMemberProfile((com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest) request,
              (io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse>) responseObserver);
          break;
        case METHODID_VALIDATE_MEMBER:
          serviceImpl.validateMember((com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest) request,
              (io.grpc.stub.StreamObserver<com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getRegisterMemberMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberRequest,
              com.gdn.training.member.infrastructure.grpc.proto.RegisterMemberResponse>(
                service, METHODID_REGISTER_MEMBER)))
        .addMethod(
          getLoginMemberMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gdn.training.member.infrastructure.grpc.proto.LoginMemberRequest,
              com.gdn.training.member.infrastructure.grpc.proto.LoginMemberResponse>(
                service, METHODID_LOGIN_MEMBER)))
        .addMethod(
          getLogoutMemberMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              com.google.protobuf.Empty>(
                service, METHODID_LOGOUT_MEMBER)))
        .addMethod(
          getGetMemberProfileMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileRequest,
              com.gdn.training.member.infrastructure.grpc.proto.GetMemberProfileResponse>(
                service, METHODID_GET_MEMBER_PROFILE)))
        .addMethod(
          getValidateMemberMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberRequest,
              com.gdn.training.member.infrastructure.grpc.proto.ValidateMemberResponse>(
                service, METHODID_VALIDATE_MEMBER)))
        .build();
  }

  private static abstract class MemberServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MemberServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.gdn.training.member.infrastructure.grpc.proto.MemberServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MemberService");
    }
  }

  private static final class MemberServiceFileDescriptorSupplier
      extends MemberServiceBaseDescriptorSupplier {
    MemberServiceFileDescriptorSupplier() {}
  }

  private static final class MemberServiceMethodDescriptorSupplier
      extends MemberServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    MemberServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MemberServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MemberServiceFileDescriptorSupplier())
              .addMethod(getRegisterMemberMethod())
              .addMethod(getLoginMemberMethod())
              .addMethod(getLogoutMemberMethod())
              .addMethod(getGetMemberProfileMethod())
              .addMethod(getValidateMemberMethod())
              .build();
        }
      }
    }
    return result;
  }
}
