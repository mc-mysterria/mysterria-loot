package dev.ua.ikeepcalm.mdlootcatwalk.api;

import dev.ua.ikeepcalm.mdlootcatwalk.MDLootCatwalk;
import dev.ua.ikeepcalm.mdlootcatwalk.api.response.*;
import dev.ua.ikeepcalm.mdlootcatwalk.manager.LootManager;
import dev.ua.ikeepcalm.mdlootcatwalk.model.LootTable;
import dev.ua.uaproject.catwalk.bridge.annotations.BridgeEventHandler;
import dev.ua.uaproject.catwalk.bridge.annotations.BridgePathParam;
import dev.ua.uaproject.catwalk.bridge.annotations.BridgeQueryParam;
import dev.ua.uaproject.catwalk.bridge.source.BridgeApiResponse;
import io.javalin.http.HttpStatus;
import io.javalin.openapi.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LootEndpoint {

    private final LootManager lootManager;

    public LootEndpoint(LootManager lootManager) {
        this.lootManager = lootManager;
    }

    @OpenApi(
            path = "/loot/tables",
            methods = HttpMethod.GET,
            summary = "Get all loot tables",
            description = "Retrieves a list of all available loot tables with basic information including name, " +
                    "namespace, item counts, and table configuration. This endpoint provides an overview of all " +
                    "loot tables configured in the MythicDungeons system for navigation and discovery purposes.",
            tags = {"Loot Tables"},
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved all loot tables",
                            content = @OpenApiContent(
                                    from = LootTablesResponse.class,
                                    mimeType = "application/json",
                                    example = """
                                            {
                                              "tables": [
                                                {
                                                  "name": "dungeon_nether",
                                                  "namespace": "dungeon_nether",
                                                  "min_items": 1,
                                                  "max_items": 1,
                                                  "item_count": 45,
                                                  "allow_duplicates": true
                                                }
                                              ]
                                            }
                                            """
                            )),
                    @OpenApiResponse(status = "500", description = "Internal server error occurred while retrieving tables",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json"))
            }
    )
    @BridgeEventHandler(requiresAuth = false, description = "Returns all loot tables", logRequests = true, scopes = {"loot"})
    public CompletableFuture<BridgeApiResponse<LootTablesResponse>> getAllLootTables() {
        try {
            Map<String, LootTable> tables = lootManager.getAllLootTables();
            
            List<LootTablesResponse.LootTableSummary> summaries = tables.values().stream()
                    .map(table -> LootTablesResponse.LootTableSummary.builder()
                            .name(table.getName())
                            .namespace(table.getNamespace())
                            .minItems(table.getMinItems())
                            .maxItems(table.getMaxItems())
                            .itemCount(table.getItems().size())
                            .allowDuplicates(table.isAllowDuplicates())
                            .build())
                    .collect(Collectors.toList());
            
            LootTablesResponse response = LootTablesResponse.builder()
                    .tables(summaries)
                    .build();
            
            return CompletableFuture.completedFuture(BridgeApiResponse.success(response));
        } catch (Exception e) {
            logError("Failed to get all loot tables", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to retrieve loot tables", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @OpenApi(
            path = "/loot/table/{name}",
            methods = HttpMethod.GET,
            summary = "Get specific loot table details",
            description = "Retrieves detailed information about a specific loot table including all items, their " +
                    "weights, chances, display names, and complete metadata. This endpoint provides comprehensive " +
                    "data for displaying complete loot table information on wiki pages.",
            tags = {"Loot Tables"},
            pathParams = {
                    @OpenApiParam(
                            name = "name",
                            type = String.class,
                            description = "Name of the loot table to retrieve",
                            example = "dungeon_nether",
                            required = true
                    )
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved loot table details",
                            content = @OpenApiContent(
                                    from = LootTableResponse.class,
                                    mimeType = "application/json",
                                    example = """
                                            {
                                              "table": {
                                                "name": "dungeon_nether",
                                                "namespace": "dungeon_nether",
                                                "min_items": 1,
                                                "max_items": 1,
                                                "allow_duplicates": true,
                                                "items": [
                                                  {
                                                    "id": "9",
                                                    "min_items": 1,
                                                    "max_items": 1,
                                                    "weight": 1,
                                                    "chance": 2.22,
                                                    "material": "PRISMARINE_SHARD",
                                                    "display_name": "Осколок Знань",
                                                    "display_name_json": {"text": "Осколок Знань", "italic": false, "color": "aqua"}
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )),
                    @OpenApiResponse(status = "404", description = "Loot table not found",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json")),
                    @OpenApiResponse(status = "500", description = "Internal server error occurred while retrieving table",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json"))
            }
    )
    @BridgeEventHandler(requiresAuth = false, description = "Returns specific loot table details", logRequests = true, scopes = {"loot"})
    public CompletableFuture<BridgeApiResponse<LootTableResponse>> getLootTable(@BridgePathParam("name") String tableName) {
        try {
            LootTable table = lootManager.getLootTable(tableName);
            
            if (table == null) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Loot table not found: " + tableName, HttpStatus.NOT_FOUND)
                );
            }
            
            LootTableResponse response = LootTableResponse.builder()
                    .table(table)
                    .build();
            
            return CompletableFuture.completedFuture(BridgeApiResponse.success(response));
        } catch (Exception e) {
            logError("Failed to get loot table: " + tableName, e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to retrieve loot table", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @OpenApi(
            path = "/loot/items",
            methods = HttpMethod.GET,
            summary = "Get all loot items",
            description = "Retrieves all loot items from all tables with their associated table information. " +
                    "This endpoint provides a comprehensive list of all obtainable items across the entire " +
                    "loot system for overview and analysis purposes.",
            tags = {"Loot Items"},
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved all loot items",
                            content = @OpenApiContent(
                                    from = LootItemsResponse.class,
                                    mimeType = "application/json",
                                    example = """
                                            {
                                              "items": [
                                                {
                                                  "table_name": "dungeon_nether",
                                                  "item": {
                                                    "id": "9",
                                                    "material": "PRISMARINE_SHARD",
                                                    "display_name": "Осколок Знань",
                                                    "chance": 2.22,
                                                    "weight": 1
                                                  }
                                                }
                                              ],
                                              "total_count": 150
                                            }
                                            """
                            )),
                    @OpenApiResponse(status = "500", description = "Internal server error occurred while retrieving items",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json"))
            }
    )
    @BridgeEventHandler(requiresAuth = false, description = "Returns all loot items", logRequests = true, scopes = {"loot"})
    public CompletableFuture<BridgeApiResponse<LootItemsResponse>> getAllItems() {
        try {
            List<LootItemsResponse.LootItemWithTable> items = lootManager.getAllItems();
            
            LootItemsResponse response = LootItemsResponse.builder()
                    .items(items)
                    .totalCount(items.size())
                    .build();
            
            return CompletableFuture.completedFuture(BridgeApiResponse.success(response));
        } catch (Exception e) {
            logError("Failed to get all items", e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to retrieve loot items", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @OpenApi(
            path = "/loot/search",
            methods = HttpMethod.GET,
            summary = "Search loot items",
            description = "Searches for loot items across all tables based on display name, material type, or " +
                    "item model. This endpoint enables dynamic filtering and discovery of specific items " +
                    "within the loot system for wiki search functionality.",
            tags = {"Loot Items"},
            queryParams = {
                    @OpenApiParam(
                            name = "name",
                            type = String.class,
                            description = "Search term to match against item names, materials, or models",
                            example = "crystal",
                            required = true
                    ),
                    @OpenApiParam(
                            name = "limit",
                            type = Integer.class,
                            description = "Maximum number of results to return (default configured in settings)",
                            example = "50"
                    )
            },
            responses = {
                    @OpenApiResponse(status = "200", description = "Successfully retrieved search results",
                            content = @OpenApiContent(
                                    from = LootSearchResponse.class,
                                    mimeType = "application/json",
                                    example = """
                                            {
                                              "results": [
                                                {
                                                  "table_name": "dungeon_nether",
                                                  "item": {
                                                    "id": "15",
                                                    "material": "PRISMARINE_CRYSTALS",
                                                    "display_name": "Кристал Есенції Вітру",
                                                    "chance": 2.22
                                                  }
                                                }
                                              ],
                                              "query": "crystal",
                                              "total_found": 1,
                                              "limited": false
                                            }
                                            """
                            )),
                    @OpenApiResponse(status = "400", description = "Search query is required",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json")),
                    @OpenApiResponse(status = "500", description = "Internal server error occurred during search",
                            content = @OpenApiContent(from = BridgeApiResponse.class, mimeType = "application/json"))
            }
    )
    @BridgeEventHandler(requiresAuth = false, description = "Searches loot items", logRequests = true, scopes = {"loot"})
    public CompletableFuture<BridgeApiResponse<LootSearchResponse>> searchItems(
            @BridgeQueryParam("name") String query,
            @BridgeQueryParam("limit") String limitParam) {
        
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Search query 'name' is required", HttpStatus.BAD_REQUEST)
            );
        }
        
        int limit = 100;
        if (limitParam != null) {
            try {
                limit = Integer.parseInt(limitParam);
                if (limit < 1) limit = 1;
                if (limit > 1000) limit = 1000;
            } catch (NumberFormatException e) {
                return CompletableFuture.completedFuture(
                        BridgeApiResponse.error("Invalid limit parameter", HttpStatus.BAD_REQUEST)
                );
            }
        }
        
        try {
            List<LootItemsResponse.LootItemWithTable> results = lootManager.searchItems(query.trim(), limit + 1);
            boolean limited = results.size() > limit;
            
            if (limited) {
                results = results.subList(0, limit);
            }
            
            LootSearchResponse response = LootSearchResponse.builder()
                    .results(results)
                    .query(query.trim())
                    .totalFound(results.size())
                    .limited(limited)
                    .build();
            
            return CompletableFuture.completedFuture(BridgeApiResponse.success(response));
        } catch (Exception e) {
            logError("Failed to search items with query: " + query, e);
            return CompletableFuture.completedFuture(
                    BridgeApiResponse.error("Failed to search loot items", HttpStatus.INTERNAL_SERVER_ERROR)
            );
        }
    }

    private void logError(String message, Throwable e) {
        MDLootCatwalk.error(message);
        if (e != null) {
            MDLootCatwalk.error(e.getMessage());
            e.printStackTrace();
        }
    }
}