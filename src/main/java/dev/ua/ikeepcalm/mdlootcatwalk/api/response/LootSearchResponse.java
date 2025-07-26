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
        description = "Response containing search results for loot items",
        properties = {
                @ApiProperty(
                        name = "results",
                        type = "array",
                        description = "List of matching loot items",
                        required = true
                ),
                @ApiProperty(
                        name = "query",
                        type = "string",
                        description = "Search query that was used",
                        required = true,
                        example = "crystal"
                ),
                @ApiProperty(
                        name = "total_found",
                        type = "integer",
                        description = "Total number of items found (may be limited by max results)",
                        required = true,
                        example = "25"
                ),
                @ApiProperty(
                        name = "limited",
                        type = "boolean",
                        description = "Whether results were limited due to max results setting",
                        required = true,
                        example = "false"
                )
        }
)
public class LootSearchResponse {
    
    @JsonProperty("results")
    private List<LootItemsResponse.LootItemWithTable> results;
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("total_found")
    private Integer totalFound;
    
    @JsonProperty("limited")
    private Boolean limited;
}