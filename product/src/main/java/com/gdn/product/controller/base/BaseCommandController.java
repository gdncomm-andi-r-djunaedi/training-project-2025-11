package com.gdn.product.controller.base;

import com.gdn.product.command.base.CommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseCommandController {

  @Autowired
  protected CommandExecutor executor;

}
