package com.denghb.eorm.generator;

import com.denghb.eorm.generator.model.TableModel;

import java.util.List;

public interface EntityGeneratorHandler {

    void onCallback(List<TableModel> data, Config config);

    void onMessage(String message);
}
