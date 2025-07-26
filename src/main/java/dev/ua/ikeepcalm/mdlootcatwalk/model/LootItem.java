package dev.ua.ikeepcalm.mdlootcatwalk.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class LootItem {
    private String id;
    private int minItems;
    private int maxItems;
    private int weight;
    private double chance;
    private String material;
    private String displayName;
    private Object displayNameJson;
    private String itemModel;
    private Map<String, Object> publicBukkitValues;
    private Map<String, Object> itemMeta;
    private Map<String, Object> rawItem;
}