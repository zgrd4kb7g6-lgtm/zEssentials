package fr.maxlego08.essentials.bot.config;

import java.util.Map;

public class FeatureManager {

    private final Configuration configuration;

    public FeatureManager(Configuration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    public boolean isEnabled(String feature) {
        try {
            Map<String, Object> features =
                    (Map<String, Object>) configuration.getConfiguration().get("features");

            if (features == null) return false;

            Object value = features.get(feature);
            if (value == null) return false;

            return Boolean.parseBoolean(value.toString());

        } catch (Exception e) {
            return false;
        }
    }
}