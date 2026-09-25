package com.starrycammod;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class vec3Mixin {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonCreator
    public vec3Mixin(
            @JsonProperty("x") double x,
            @JsonProperty("y") double y,
            @JsonProperty("z") double z
    ) {}
}