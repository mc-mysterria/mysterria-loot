package dev.ua.ikeepcalm.mdlootcatwalk.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.mdlootcatwalk.model.LootItem;
import dev.ua.uaproject.catwalk.bridge.annotations.ApiProperty;
import dev.ua.uaproject.catwalk.bridge.annotations.ApiSchema;
import io.javalin.openapi.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSchema
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiSchema(
        description = "Response containing loot items from all tables",
        properties = {
                @ApiProperty(
                        name = "items",
                        type = "array",
                        description = "List of all loot items across all tables",
                        required = true
                ),
                @ApiProperty(
                        name = "total_count",
                        type = "integer",
                        description = "Total number of items found",
                        required = true,
                        example = "150"
                )
        }
)
public class LootItemsResponse {
    
    @JsonProperty("items")
    private List<LootItemWithTable> items;
    
    @JsonProperty("total_count")
    private Integer totalCount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LootItemWithTable {
        @JsonProperty("table_name")
        private String tableName;
        
        @JsonProperty("item")
        private LootItem item;
    }
}