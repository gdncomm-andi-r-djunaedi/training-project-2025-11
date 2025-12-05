package com.gdn.project.waroenk.gateway.service;

import com.gdn.project.waroenk.gateway.config.GrpcChannelConfig;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for gRPC service stubs and method descriptors.
 * Uses reflection to dynamically invoke gRPC methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcServiceRegistry {

    private final GrpcChannelConfig channelConfig;

    // Cache for proto message builders
    private final Map<String, Class<? extends Message>> messageTypeCache = new ConcurrentHashMap<>();

    // JSON parser/printer for protobuf
    private final JsonFormat.Parser jsonParser = JsonFormat.parser().ignoringUnknownFields();
    private final JsonFormat.Printer jsonPrinter = JsonFormat.printer()
            .preservingProtoFieldNames()
            .includingDefaultValueFields();

    @PostConstruct
    public void init() {
        log.info("Initializing gRPC Service Registry");
        // Pre-register known message types from grpc-contract
        registerKnownMessageTypes();
    }

    private void registerKnownMessageTypes() {
        // Register common message types
        registerMessageType("com.gdn.project.waroenk.common.Id");
        registerMessageType("com.gdn.project.waroenk.common.Empty");
        registerMessageType("com.gdn.project.waroenk.common.Basic");
        
        // Register member service types
        registerMessageType("com.gdn.project.waroenk.member.CreateUserRequest");
        registerMessageType("com.gdn.project.waroenk.member.CreateUserResponse");
        registerMessageType("com.gdn.project.waroenk.member.UserData");
        registerMessageType("com.gdn.project.waroenk.member.AuthenticateRequest");
        registerMessageType("com.gdn.project.waroenk.member.UserTokenResponse");
        registerMessageType("com.gdn.project.waroenk.member.PhoneOrEmailRequest");
        registerMessageType("com.gdn.project.waroenk.member.FilterUserRequest");
        registerMessageType("com.gdn.project.waroenk.member.MultipleUserResponse");
        registerMessageType("com.gdn.project.waroenk.member.UpdateUserRequest");
        
        // Register address types
        registerMessageType("com.gdn.project.waroenk.member.AddressData");
        registerMessageType("com.gdn.project.waroenk.member.UpsertAddressRequest");
        registerMessageType("com.gdn.project.waroenk.member.FilterAddressRequest");
        registerMessageType("com.gdn.project.waroenk.member.MultipleAddressResponse");
        registerMessageType("com.gdn.project.waroenk.member.SetDefaultAddressRequest");
        
        // Register catalog service types
        registerMessageType("com.gdn.project.waroenk.catalog.ProductData");
        registerMessageType("com.gdn.project.waroenk.catalog.CreateProductRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.UpdateProductRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.FilterProductRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.MultipleProductResponse");
        registerMessageType("com.gdn.project.waroenk.catalog.FindProductBySkuRequest");
        
        // Register brand types
        registerMessageType("com.gdn.project.waroenk.catalog.BrandData");
        registerMessageType("com.gdn.project.waroenk.catalog.CreateBrandRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.UpdateBrandRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.FilterBrandRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.MultipleBrandResponse");
        
        // Register merchant types
        registerMessageType("com.gdn.project.waroenk.catalog.MerchantData");
        registerMessageType("com.gdn.project.waroenk.catalog.CreateMerchantRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.UpdateMerchantRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.FilterMerchantRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.MultipleMerchantResponse");
        registerMessageType("com.gdn.project.waroenk.catalog.FindMerchantByCodeRequest");
        
        // Register category types
        registerMessageType("com.gdn.project.waroenk.catalog.CategoryData");
        registerMessageType("com.gdn.project.waroenk.catalog.CreateCategoryRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.UpdateCategoryRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.FilterCategoryRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.MultipleCategoryResponse");
        
        // Register search types
        registerMessageType("com.gdn.project.waroenk.catalog.CombinedSearchRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.CombinedSearchResponse");
        registerMessageType("com.gdn.project.waroenk.catalog.SearchProductsRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.SearchProductsResponse");
        registerMessageType("com.gdn.project.waroenk.catalog.SearchMerchantsRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.SearchMerchantsResponse");
        
        // Register inventory types
        registerMessageType("com.gdn.project.waroenk.catalog.InventoryData");
        registerMessageType("com.gdn.project.waroenk.catalog.UpdateInventoryRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.BulkUpdateInventoryRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.BulkInventoryResponse");
        
        // Register system parameter types
        registerMessageType("com.gdn.project.waroenk.member.SystemParameterData");
        registerMessageType("com.gdn.project.waroenk.member.UpsertSystemParameterRequest");
        registerMessageType("com.gdn.project.waroenk.member.FilterSystemParameterRequest");
        registerMessageType("com.gdn.project.waroenk.member.MultipleSystemParameterResponse");
        registerMessageType("com.gdn.project.waroenk.member.MultipleUpsertSystemParameterRequest");
        
        registerMessageType("com.gdn.project.waroenk.catalog.SystemParameterData");
        registerMessageType("com.gdn.project.waroenk.catalog.UpsertSystemParameterRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.FilterSystemParameterRequest");
        registerMessageType("com.gdn.project.waroenk.catalog.MultipleSystemParameterResponse");

        // Register cart service types
        registerMessageType("com.gdn.project.waroenk.cart.CartData");
        registerMessageType("com.gdn.project.waroenk.cart.CartItemData");
        registerMessageType("com.gdn.project.waroenk.cart.CartItemInput");
        registerMessageType("com.gdn.project.waroenk.cart.GetCartRequest");
        registerMessageType("com.gdn.project.waroenk.cart.AddCartItemRequest");
        registerMessageType("com.gdn.project.waroenk.cart.BulkAddCartItemsRequest");
        registerMessageType("com.gdn.project.waroenk.cart.RemoveCartItemRequest");
        registerMessageType("com.gdn.project.waroenk.cart.BulkRemoveCartItemsRequest");
        registerMessageType("com.gdn.project.waroenk.cart.UpdateCartItemRequest");
        registerMessageType("com.gdn.project.waroenk.cart.ClearCartRequest");
        
        // Register checkout types
        registerMessageType("com.gdn.project.waroenk.cart.CheckoutData");
        registerMessageType("com.gdn.project.waroenk.cart.ValidateCheckoutRequest");
        registerMessageType("com.gdn.project.waroenk.cart.ValidateCheckoutResponse");
        registerMessageType("com.gdn.project.waroenk.cart.GetCheckoutRequest");
        registerMessageType("com.gdn.project.waroenk.cart.FinalizeCheckoutRequest");
        registerMessageType("com.gdn.project.waroenk.cart.FinalizeCheckoutResponse");
        registerMessageType("com.gdn.project.waroenk.cart.InvalidateCheckoutRequest");

        log.info("Registered {} message types", messageTypeCache.size());
    }

    @SuppressWarnings("unchecked")
    private void registerMessageType(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (Message.class.isAssignableFrom(clazz)) {
                messageTypeCache.put(className, (Class<? extends Message>) clazz);
                // Also register by simple name
                messageTypeCache.put(clazz.getSimpleName(), (Class<? extends Message>) clazz);
            }
        } catch (ClassNotFoundException e) {
            log.debug("Message type not found (may not be used): {}", className);
        }
    }

    /**
     * Get the channel for a service
     */
    public Channel getChannel(String serviceName) {
        return channelConfig.getChannel(serviceName);
    }

    /**
     * Parse JSON to a protobuf message
     */
    public Message parseJsonToMessage(String json, String messageType) throws InvalidProtocolBufferException {
        Class<? extends Message> messageClass = messageTypeCache.get(messageType);
        if (messageClass == null) {
            throw new IllegalArgumentException("Unknown message type: " + messageType);
        }

        try {
            Method newBuilderMethod = messageClass.getMethod("newBuilder");
            Message.Builder builder = (Message.Builder) newBuilderMethod.invoke(null);
            jsonParser.merge(json, builder);
            return builder.build();
        } catch (Exception e) {
            throw new InvalidProtocolBufferException("Failed to parse JSON to " + messageType + ": " + e.getMessage());
        }
    }

    /**
     * Convert protobuf message to JSON
     */
    public String messageToJson(Message message) throws InvalidProtocolBufferException {
        return jsonPrinter.print(message);
    }

    /**
     * Get default instance of a message type
     */
    public Message getDefaultInstance(String messageType) {
        Class<? extends Message> messageClass = messageTypeCache.get(messageType);
        if (messageClass == null) {
            throw new IllegalArgumentException("Unknown message type: " + messageType);
        }

        try {
            Method getDefaultInstance = messageClass.getMethod("getDefaultInstance");
            return (Message) getDefaultInstance.invoke(null);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get default instance for " + messageType, e);
        }
    }

    /**
     * Get the descriptor for a message type
     */
    public Descriptors.Descriptor getDescriptor(String messageType) {
        Message defaultInstance = getDefaultInstance(messageType);
        return defaultInstance.getDescriptorForType();
    }

    /**
     * Check if a message type is registered
     */
    public boolean hasMessageType(String messageType) {
        return messageTypeCache.containsKey(messageType);
    }
}

