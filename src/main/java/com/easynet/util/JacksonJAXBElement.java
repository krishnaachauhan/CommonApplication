package com.easynet.util;


import com.fasterxml.jackson.annotation.JsonValue;

public interface JacksonJAXBElement {

    @JsonValue
    Object getValue();
}
