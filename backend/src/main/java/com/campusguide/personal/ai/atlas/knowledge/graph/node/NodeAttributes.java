package com.campusguide.personal.ai.atlas.knowledge.graph.node;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.*;

/**
 * Flexible, type-safe attribute container for KnowledgeNode properties.
 */
@EqualsAndHashCode
@ToString
public class NodeAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Object> properties;

    public NodeAttributes() {
        this.properties = new HashMap<>();
    }

    public NodeAttributes(Map<String, Object> initialProperties) {
        this.properties = new HashMap<>();
        if (initialProperties != null) {
            this.properties.putAll(initialProperties);
        }
    }

    public static NodeAttributes empty() {
        return new NodeAttributes();
    }

    public static NodeAttributes of(Map<String, Object> props) {
        return new NodeAttributes(props);
    }

    public NodeAttributes put(String key, Object value) {
        if (key != null && !key.isBlank()) {
            if (value == null) {
                properties.remove(key);
            } else {
                properties.put(key, value);
            }
        }
        return this;
    }

    public Object get(String key) {
        return properties.get(key);
    }

    public String getString(String key) {
        Object val = properties.get(key);
        return val != null ? val.toString() : null;
    }

    public String getStringOrDefault(String key, String defaultValue) {
        String val = getString(key);
        return val != null ? val : defaultValue;
    }

    public Integer getInteger(String key) {
        Object val = properties.get(key);
        if (val instanceof Number number) {
            return number.intValue();
        } else if (val instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public Double getDouble(String key) {
        Object val = properties.get(key);
        if (val instanceof Number number) {
            return number.doubleValue();
        } else if (val instanceof String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public Boolean getBoolean(String key) {
        Object val = properties.get(key);
        if (val instanceof Boolean b) {
            return b;
        } else if (val instanceof String str) {
            return Boolean.parseBoolean(str);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Object> getList(String key) {
        Object val = properties.get(key);
        if (val instanceof List<?> list) {
            return (List<Object>) list;
        }
        return Collections.emptyList();
    }

    public boolean has(String key) {
        return properties.containsKey(key);
    }

    public void remove(String key) {
        properties.remove(key);
    }

    public NodeAttributes merge(NodeAttributes other) {
        if (other != null && other.properties != null) {
            this.properties.putAll(other.properties);
        }
        return this;
    }

    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(new HashMap<>(properties));
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public int size() {
        return properties.size();
    }
}
