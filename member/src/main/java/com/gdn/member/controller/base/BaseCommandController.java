package com.gdn.member.controller.base;

import com.gdn.member.command.base.CommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseCommandController {

  @Autowired
  protected CommandExecutor executor;

}
