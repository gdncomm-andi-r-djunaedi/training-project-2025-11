package com.gdn.project.waroenk.catalog.service;

import com.gdn.project.waroenk.catalog.FilterMerchantRequest;
import com.gdn.project.waroenk.catalog.MultipleMerchantResponse;
import com.gdn.project.waroenk.catalog.entity.Merchant;

public interface MerchantService {
  Merchant createMerchant(Merchant merchant);
  Merchant updateMerchant(String id, Merchant merchant);
  Merchant findMerchantById(String id);
  Merchant findMerchantByCode(String code);
  boolean deleteMerchant(String id);
  MultipleMerchantResponse filterMerchants(FilterMerchantRequest request);
}







