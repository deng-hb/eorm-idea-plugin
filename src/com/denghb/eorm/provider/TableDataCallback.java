package com.denghb.eorm.provider;

import com.denghb.eorm.generator.model.TableModel;

import java.util.List;

public interface TableDataCallback {

    void onData(List<TableModel> tables);

    void onMessage(String message);
}
