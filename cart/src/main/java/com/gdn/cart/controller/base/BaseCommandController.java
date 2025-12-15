package com.gdn.cart.controller.base;

import com.gdn.cart.command.base.CommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseCommandController {

  @Autowired
  protected CommandExecutor executor;
}

