package dev.ua.ikeepcalm.mdlootcatwalk.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        description = "Response containing all available loot tables",
        properties = {
                @ApiProperty(
                        name = "tables",
                        type = "array",
                        description = "List of all loot table names and basic information",
                        required = true,
                        example = "[{\"name\": \"dungeon_nether\", \"namespace\": \"dungeon_nether\", \"itemCount\": 45}]"
                )
        }
)
public class LootTablesResponse {
    
    @JsonProperty("tables")
    private List<LootTableSummary> tables;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LootTableSummary {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("namespace")
        private String namespace;
        
        @JsonProperty("min_items")
        private Integer minItems;
        
        @JsonProperty("max_items")
        private Integer maxItems;
        
        @JsonProperty("item_count")
        private Integer itemCount;
        
        @JsonProperty("allow_duplicates")
        private Boolean allowDuplicates;
    }
}