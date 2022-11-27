package com.bittoo.search.client.model;

import lombok.Data;

import java.util.Map;

@Data
public class Hit {
    Map<String, Object> source;
}