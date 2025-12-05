package com.gdn.project.waroenk.member.repository;

import com.gdn.project.waroenk.member.dto.SortByDto;
import com.gdn.project.waroenk.member.entity.SystemParameter;

import java.util.List;

public interface SystemParameterCustomRepository {
  List<SystemParameter> findSystemParametersLike(String variable, String cursor, int size, SortByDto sortBy);

  Long countAll();
}
