package com.gdn.member.command.base;

public interface Command<R, T> {

  T execute(R commandRequest);
}
