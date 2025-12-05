package com.gdn.faurihakim.product.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.product.command.model.SearchProductCommandRequest;
import com.gdn.faurihakim.product.web.model.response.SearchProductWebResponse;

public interface SearchProductCommand extends Command<SearchProductCommandRequest, SearchProductWebResponse> {
}
