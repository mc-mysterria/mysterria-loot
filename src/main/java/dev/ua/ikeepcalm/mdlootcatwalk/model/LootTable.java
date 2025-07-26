package dev.ua.ikeepcalm.mdlootcatwalk.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class LootTable {
    private String name;
    private String namespace;
    private int minItems;
    private int maxItems;
    private boolean allowDuplicates;
    private List<LootItem> items;
    private Map<String, Object> rawData;
}