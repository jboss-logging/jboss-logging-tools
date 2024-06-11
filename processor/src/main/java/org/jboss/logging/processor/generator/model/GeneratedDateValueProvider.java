package org.jboss.logging.processor.generator.model;

import java.util.Locale;
import java.util.Map;

import org.jboss.logging.processor.apt.LoggingToolsProcessor;

public abstract class GeneratedDateValueProvider {
    enum Type {
        DEFAULT,
        NONE,
        FIXED;
    }

    abstract String date();

    public static GeneratedDateValueProvider of(Map<String, String> options) {
        Type type = Type.valueOf(options.getOrDefault(LoggingToolsProcessor.GENERATED_DATE_VALUE_PROVIDER, Type.DEFAULT.name())
                .toUpperCase(Locale.ROOT));
        switch (type) {
            case DEFAULT:
                return DefaultGeneratedDateValueProvider.INSTANCE;
            case NONE:
                return NoneGeneratedDateValueProvider.INSTANCE;
            case FIXED:
                String date = options.get(LoggingToolsProcessor.GENERATED_DATE_VALUE_PROVIDER_DATE);
                if (date == null) {
                    throw new IllegalArgumentException(
                            "Fixed date generated date value provider is requested, but the date is not specified. "
                                    + "Use " + LoggingToolsProcessor.GENERATED_DATE_VALUE_PROVIDER_DATE
                                    + " annotation processor option to provide the date.");
                }
                return new FixedGeneratedDateValueProvider(date);
            default:
                throw new IllegalStateException("Unknown generated date value provider type: " + type);
        }
    }

    private static class DefaultGeneratedDateValueProvider extends GeneratedDateValueProvider {

        private static final DefaultGeneratedDateValueProvider INSTANCE = new DefaultGeneratedDateValueProvider();

        @Override
        String date() {
            return ClassModelHelper.generatedDateValue();
        }
    }

    private static class NoneGeneratedDateValueProvider extends GeneratedDateValueProvider {

        private static final NoneGeneratedDateValueProvider INSTANCE = new NoneGeneratedDateValueProvider();

        @Override
        String date() {
            return "";
        }
    }

    private static class FixedGeneratedDateValueProvider extends GeneratedDateValueProvider {

        private final String date;

        private FixedGeneratedDateValueProvider(String date) {
            this.date = date;
        }

        @Override
        String date() {
            return date;
        }
    }
}
