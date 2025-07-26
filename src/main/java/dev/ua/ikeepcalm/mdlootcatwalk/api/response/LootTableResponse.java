package dev.ua.ikeepcalm.mdlootcatwalk.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.mdlootcatwalk.model.LootTable;
import dev.ua.uaproject.catwalk.bridge.annotations.ApiProperty;
import dev.ua.uaproject.catwalk.bridge.annotations.ApiSchema;
import io.javalin.openapi.JsonSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSchema
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiSchema(
        description = "Response containing detailed loot table information",
        properties = {
                @ApiProperty(
                        name = "table",
                        type = "object",
                        description = "Complete loot table with all items and their details",
                        required = true
                )
        }
)
public class LootTableResponse {
    
    @JsonProperty("table")
    private LootTable table;
}