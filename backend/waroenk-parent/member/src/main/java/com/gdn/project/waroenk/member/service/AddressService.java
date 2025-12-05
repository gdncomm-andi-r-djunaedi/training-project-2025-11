package com.gdn.project.waroenk.member.service;

import com.gdn.project.waroenk.member.FilterAddressRequest;
import com.gdn.project.waroenk.member.MultipleAddressResponse;
import com.gdn.project.waroenk.member.entity.Address;

public interface AddressService {

  Address findAddressById(String id);

  Address findUserAddressByLabel(String userId, String label);

  Address createAddress(String userId, Address address);

  MultipleAddressResponse filterUserAddress(FilterAddressRequest request);

  boolean setDefaultAddress(String userId, String label);

  boolean deleteUserAddress(String id);
}
