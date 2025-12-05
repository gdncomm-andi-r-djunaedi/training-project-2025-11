package com.gdn.project.waroenk.catalog.controller.http;

import com.gdn.project.waroenk.catalog.CategoryData;
import com.gdn.project.waroenk.catalog.CategoryServiceGrpc;
import com.gdn.project.waroenk.catalog.CategoryTreeResponse;
import com.gdn.project.waroenk.catalog.FilterCategoryRequest;
import com.gdn.project.waroenk.catalog.FindCategoryBySlugRequest;
import com.gdn.project.waroenk.catalog.MultipleCategoryResponse;
import com.gdn.project.waroenk.catalog.constant.ApiConstant;
import com.gdn.project.waroenk.catalog.dto.BasicDto;
import com.gdn.project.waroenk.catalog.dto.category.CategoryResponseDto;
import com.gdn.project.waroenk.catalog.dto.category.CategoryTreeNodeDto;
import com.gdn.project.waroenk.catalog.dto.category.CreateCategoryRequestDto;
import com.gdn.project.waroenk.catalog.dto.category.ListOfCategoryResponseDto;
import com.gdn.project.waroenk.catalog.dto.category.UpdateCategoryRequestDto;
import com.gdn.project.waroenk.catalog.mapper.CategoryMapper;
import com.gdn.project.waroenk.common.Basic;
import com.gdn.project.waroenk.common.Empty;
import com.gdn.project.waroenk.common.Id;
import com.gdn.project.waroenk.common.SortBy;
import jakarta.validation.Valid;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(ApiConstant.BASE_PATH)
@RestController("catalogHttpCategoryController")
public class CategoryController {
  private static final CategoryMapper mapper = CategoryMapper.INSTANCE;
  private final CategoryServiceGrpc.CategoryServiceBlockingStub grpcClient;

  @Autowired
  public CategoryController(@GrpcClient("catalog-service") CategoryServiceGrpc.CategoryServiceBlockingStub grpcClient) {
    this.grpcClient = grpcClient;
  }

  @PostMapping("/category")
  public CategoryResponseDto createCategory(@Valid @RequestBody CreateCategoryRequestDto requestDto) {
    CategoryData response = grpcClient.createCategory(mapper.toRequestGrpc(requestDto));
    return mapper.toResponseDto(response);
  }

  @PutMapping("/category/{id}")
  public CategoryResponseDto updateCategory(@PathVariable String id, @RequestBody UpdateCategoryRequestDto requestDto) {
    CategoryData response = grpcClient.updateCategory(mapper.toRequestGrpc(id, requestDto));
    return mapper.toResponseDto(response);
  }

  @DeleteMapping("/category/{id}")
  public BasicDto deleteCategory(@PathVariable String id) {
    Basic response = grpcClient.deleteCategory(Id.newBuilder().setValue(id).build());
    return mapper.toBasicDto(response);
  }

  @GetMapping("/category/{id}")
  public CategoryResponseDto findCategoryById(@PathVariable String id) {
    CategoryData response = grpcClient.findCategoryById(Id.newBuilder().setValue(id).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/category/by-slug")
  public CategoryResponseDto findCategoryBySlug(@RequestParam String slug) {
    CategoryData response = grpcClient.findCategoryBySlug(FindCategoryBySlugRequest.newBuilder().setSlug(slug).build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/category/filter")
  public ListOfCategoryResponseDto filterCategories(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String slug,
      @RequestParam(required = false) String parentId,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "id") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
    FilterCategoryRequest.Builder builder = FilterCategoryRequest.newBuilder().setSize(size);
    if (StringUtils.isNotBlank(name)) builder.setName(name);
    if (StringUtils.isNotBlank(slug)) builder.setSlug(slug);
    if (StringUtils.isNotBlank(parentId)) builder.setParentId(parentId);
    if (StringUtils.isNotBlank(cursor)) builder.setCursor(cursor);
    builder.setSortBy(SortBy.newBuilder().setField(sortBy).setDirection(sortDirection).build());
    
    MultipleCategoryResponse response = grpcClient.filterCategory(builder.build());
    return mapper.toResponseDto(response);
  }

  @GetMapping("/category/tree")
  public List<CategoryTreeNodeDto> getCategoryTree() {
    CategoryTreeResponse response = grpcClient.getCategoryTree(Empty.getDefaultInstance());
    return mapper.toTreeNodeDtoList(response);
  }
}






