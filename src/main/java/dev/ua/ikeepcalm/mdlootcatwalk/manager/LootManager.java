package dev.ua.ikeepcalm.mdlootcatwalk.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ua.ikeepcalm.mdlootcatwalk.MDLootCatwalk;
import dev.ua.ikeepcalm.mdlootcatwalk.api.response.LootItemsResponse;
import dev.ua.ikeepcalm.mdlootcatwalk.config.LootConfig;
import dev.ua.ikeepcalm.mdlootcatwalk.model.LootItem;
import dev.ua.ikeepcalm.mdlootcatwalk.model.LootTable;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class LootManager {
    private final MDLootCatwalk plugin;
    private final LootConfig config;
    private final ObjectMapper objectMapper;

    private final Map<String, LootTable> lootTables = new ConcurrentHashMap<>();
    private BukkitTask refreshTask;
    private long lastModified = 0;

    public LootManager(MDLootCatwalk plugin, LootConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.objectMapper = new ObjectMapper();

        loadLootTables();

        if (config.getRefreshIntervalMinutes() > 0) {
            startRefreshTask();
        }
    }

    public void stop() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    private void startRefreshTask() {
        if (refreshTask != null) {
            refreshTask.cancel();
        }

        long intervalTicks = 20L * 60L * config.getRefreshIntervalMinutes();
        refreshTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkAndReloadIfChanged, intervalTicks, intervalTicks);
    }

    private void checkAndReloadIfChanged() {
        Path filePath = Paths.get(config.getLootTablesFile());
        if (Files.exists(filePath)) {
            try {
                long currentModified = Files.getLastModifiedTime(filePath).toMillis();
                if (currentModified > lastModified) {
                    MDLootCatwalk.log("Loot tables file changed, reloading...");
                    loadLootTables();
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check file modification time", e);
            }
        }
    }

    public boolean reloadLootTables() {
        return loadLootTables();
    }

    @SuppressWarnings("unchecked")
    private boolean loadLootTables() {
        Path filePath = Paths.get(config.getLootTablesFile());

        if (!Files.exists(filePath)) {
            MDLootCatwalk.error("Loot tables file not found: " + config.getLootTablesFile());
            return false;
        }

        try {
            lastModified = Files.getLastModifiedTime(filePath).toMillis();

            Yaml yaml = new Yaml();
            Map<String, Object> data;

            try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                data = yaml.load(fis);
            }

            if (data == null || !data.containsKey("Tables")) {
                MDLootCatwalk.error("Invalid loot tables file format - missing 'Tables' section");
                return false;
            }

            Map<String, Object> tables = (Map<String, Object>) data.get("Tables");
            lootTables.clear();

            for (Map.Entry<String, Object> entry : tables.entrySet()) {
                String tableName = entry.getKey();
                Map<String, Object> tableData = (Map<String, Object>) entry.getValue();

                try {
                    MDLootCatwalk.log("Parsing loot table: " + tableName);
                    LootTable lootTable = parseLootTable(tableName, tableData);
                    lootTables.put(tableName, lootTable);
                    MDLootCatwalk.log("Successfully parsed table: " + tableName + " with " + lootTable.getItems().size() + " items");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to parse loot table: " + tableName, e);
                }
            }

            MDLootCatwalk.log("Loaded " + lootTables.size() + " loot tables");
            return true;

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load loot tables", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private LootTable parseLootTable(String name, Map<String, Object> tableData) {
        String namespace = (String) tableData.getOrDefault("namespace", name);
        int minItems = ((Number) tableData.getOrDefault("minItems", 1)).intValue();
        int maxItems = ((Number) tableData.getOrDefault("maxItems", 1)).intValue();
        Object allowDuplicatesObj = tableData.get("allowDuplicates");
        boolean allowDuplicates = allowDuplicatesObj instanceof Boolean ? (Boolean) allowDuplicatesObj : false;

        Map<Object, Object> lootItems = (Map<Object, Object>) tableData.get("lootItems");
        List<LootItem> items = new ArrayList<>();

        if (lootItems != null) {
            int totalWeight = calculateTotalWeight(lootItems);

            for (Map.Entry<Object, Object> itemEntry : lootItems.entrySet()) {
                String itemId = String.valueOf(itemEntry.getKey());
                Map<String, Object> itemData = (Map<String, Object>) itemEntry.getValue();

                try {
                    LootItem lootItem = parseLootItem(itemId, itemData, totalWeight);
                    items.add(lootItem);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to parse loot item " + itemId + " in table " + name, e);
                }
            }
        }

        return LootTable.builder()
                .name(name)
                .namespace(namespace)
                .minItems(minItems)
                .maxItems(maxItems)
                .allowDuplicates(allowDuplicates)
                .items(items)
                .rawData(tableData)
                .build();
    }

    @SuppressWarnings("unchecked")
    private int calculateTotalWeight(Map<Object, Object> lootItems) {
        int totalWeight = 0;
        for (Object itemObj : lootItems.values()) {
            Map<String, Object> itemData = (Map<String, Object>) itemObj;
            totalWeight += ((Number) itemData.getOrDefault("weight", 1)).intValue();
        }
        return totalWeight;
    }

    @SuppressWarnings("unchecked")
    private LootItem parseLootItem(String id, Map<String, Object> itemData, int totalWeight) {
        int minItems = itemData.get("minItems") instanceof Number ? ((Number) itemData.get("minItems")).intValue() : 1;
        int maxItems = itemData.get("maxItems") instanceof Number ? ((Number) itemData.get("maxItems")).intValue() : 1;
        int weight = itemData.get("weight") instanceof Number ? ((Number) itemData.get("weight")).intValue() : 1;
        double chance = totalWeight > 0 ? (double) weight / totalWeight * 100.0 : 0.0;

        Map<String, Object> item = (Map<String, Object>) itemData.get("item");
        String material = "UNKNOWN";
        String displayName = null;
        Object displayNameJson = null;
        String itemModel = null;
        Map<String, Object> publicBukkitValues = null;
        Map<String, Object> itemMeta = null;

        if (item != null) {
            material = item.get("type") instanceof String ? (String) item.get("type") : "UNKNOWN";

            Map<String, Object> meta = (Map<String, Object>) item.get("meta");
            if (meta != null) {
                itemMeta = meta;
                Object displayNameObj = meta.get("display-name");
                if (displayNameObj instanceof String) {
                    String displayNameStr = (String) displayNameObj;
                    displayName = extractTextFromJson(displayNameStr);
                    displayNameJson = parseJsonSafely(displayNameStr);
                }

                Object itemModelObj = meta.get("item-model");
                if (itemModelObj instanceof String) {
                    itemModel = (String) itemModelObj;
                }

                Object publicBukkitValuesObj = meta.get("PublicBukkitValues");
                if (publicBukkitValuesObj instanceof String) {
                    publicBukkitValues = parseYamlSafely((String) publicBukkitValuesObj);
                }
            }
        }

        return LootItem.builder()
                .id(id)
                .minItems(minItems)
                .maxItems(maxItems)
                .weight(weight)
                .chance(Math.round(chance * 100.0) / 100.0)
                .material(material)
                .displayName(displayName)
                .displayNameJson(displayNameJson)
                .itemModel(itemModel)
                .publicBukkitValues(publicBukkitValues)
                .itemMeta(itemMeta)
                .rawItem(item)
                .build();
    }

    private String extractTextFromJson(String jsonStr) {
        try {
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                return null;
            }

            Map<String, Object> json = objectMapper.readValue(jsonStr, Map.class);
            Object textObj = json.get("text");
            return textObj instanceof String ? (String) textObj : jsonStr;
        } catch (Exception e) {
            return jsonStr;
        }
    }

    private Object parseJsonSafely(String jsonStr) {
        try {
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(jsonStr, Object.class);
        } catch (JsonProcessingException e) {
            return jsonStr;
        }
    }

    private Map<String, Object> parseYamlSafely(String yamlStr) {
        try {
            if (yamlStr == null || yamlStr.trim().isEmpty()) {
                return new HashMap<>();
            }
            Yaml yaml = new Yaml();
            Object result = yaml.load(yamlStr);
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
            return Map.of("raw", yamlStr);
        } catch (Exception e) {
            return Map.of("raw", yamlStr, "error", e.getMessage());
        }
    }

    public Map<String, LootTable> getAllLootTables() {
        return new HashMap<>(lootTables);
    }

    public LootTable getLootTable(String name) {
        return lootTables.get(name);
    }

    public List<LootItemsResponse.LootItemWithTable> getAllItems() {
        List<LootItemsResponse.LootItemWithTable> allItems = new ArrayList<>();

        for (LootTable table : lootTables.values()) {
            for (LootItem item : table.getItems()) {
                allItems.add(LootItemsResponse.LootItemWithTable.builder()
                        .tableName(table.getName())
                        .item(item)
                        .build());
            }
        }

        return allItems;
    }

    public List<LootItemsResponse.LootItemWithTable> searchItems(String query, int maxResults) {
        String lowerQuery = query.toLowerCase();
        List<LootItemsResponse.LootItemWithTable> results = new ArrayList<>();

        for (LootTable table : lootTables.values()) {
            for (LootItem item : table.getItems()) {
                boolean matches = false;

                if (item.getDisplayName() != null && item.getDisplayName().toLowerCase().contains(lowerQuery)) {
                    matches = true;
                } else if (item.getMaterial().toLowerCase().contains(lowerQuery)) {
                    matches = true;
                } else if (item.getItemModel() != null && item.getItemModel().toLowerCase().contains(lowerQuery)) {
                    matches = true;
                }

                if (matches) {
                    results.add(LootItemsResponse.LootItemWithTable.builder()
                            .tableName(table.getName())
                            .item(item)
                            .build());

                    if (results.size() >= maxResults) {
                        break;
                    }
                }
            }

            if (results.size() >= maxResults) {
                break;
            }
        }

        return results;
    }
}