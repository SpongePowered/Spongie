/*
 * Eduardo, an IRC bot framework
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) Eduardo team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.eduardo.util.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.emitter.ScalarAnalysis;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class YamlConfig implements ConfigFile {

    private static final Logger log = LoggerFactory.getLogger(YamlConfig.class);

    private final File file;
    private final Yaml yaml;
    private Config config = new ConfigObject().toConfig();

    private YamlConfig(File file) {
        checkNotNull(file);

        this.file = file;

        FancyDumperOptions options = new FancyDumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(FlowStyle.BLOCK);
        FancyRepresenter representer = new FancyRepresenter();
        representer.setDefaultFlowStyle(FlowStyle.BLOCK);

        yaml = new Yaml(new CustomConstructor(), representer, options);
    }

    @Override
    public boolean load() {
        try (Closer closer = Closer.create()) {
            FileInputStream fis = closer.register(new FileInputStream(file));
            BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
            UnicodeReader reader = closer.register(new UnicodeReader(bis));
            Object result = yaml.load(reader);

            this.config = ((ConfigObject) result).toConfig();
            return true;
        } catch (FileNotFoundException ignored) {
        } catch (YAMLException | IOException e) {
            log.warn("Failed to read the configuration at " + file.getAbsolutePath(), e);
        }

        return false;
    }

    @Override
    public boolean save() {
        String output = yaml.dump(config.toObject());

        try (Closer closer = Closer.create()) {
            FileOutputStream fis = closer.register(new FileOutputStream(file));
            BufferedOutputStream bis = closer.register(new BufferedOutputStream(fis));
            OutputStreamWriter writer = closer.register(new OutputStreamWriter(bis, "UTF-8"));
            writer.write(output);

            return true;
        } catch (YAMLException | IOException e) {
            log.warn("Failed to write configuration to " + file.getAbsolutePath(), e);
            return false;
        }
    }

    @Override
    @Nullable
    public Object get(String path) {
        return config.get(path);
    }

    @Override
    @Nullable
    public Object put(String path, Object value) {
        return config.put(path, value);
    }

    @Override
    public boolean getBoolean(String path, boolean fallback) {
        return config.getBoolean(path, fallback);
    }

    @Override
    public Number getNumber(String path, Number fallback) {
        return config.getNumber(path, fallback);
    }

    @Override
    public int getInt(String path, int fallback) {
        return config.getInt(path, fallback);
    }

    @Override
    public long getLong(String path, long fallback) {
        return config.getLong(path, fallback);
    }

    @Override
    public double getDouble(String path, double fallback) {
        return config.getDouble(path, fallback);
    }

    @Override
    public String getString(String path, String fallback) {
        return config.getString(path, fallback);
    }

    @Override
    public ConfigObject getObject(String path) {
        return config.getObject(path);
    }

    @Override
    public ConfigList getList(String path) {
        return config.getList(path);
    }

    @Override
    public <T> ImmutableList<T> getList(String path, Class<T> type) {
        return config.getList(path, type);
    }

    @Override
    public <T> ImmutableMap<String, T> getMap(String path, Class<T> type) {
        return config.getMap(path, type);
    }

    @Override
    public Config getConfig(String path) {
        return config.getConfig(path);
    }

    @Override
    public Supplier<Boolean> booleanAt(String path, boolean fallback) {
        return config.booleanAt(path, fallback);
    }

    @Override
    public Supplier<Number> numberAt(String path, Number fallback) {
        return config.numberAt(path, fallback);
    }

    @Override
    public Supplier<Integer> intAt(String path, Integer fallback) {
        return config.intAt(path, fallback);
    }

    @Override
    public Supplier<Long> longAt(String path, Long fallback) {
        return config.longAt(path, fallback);
    }

    @Override
    public Supplier<Double> doubleAt(String path, Double fallback) {
        return config.doubleAt(path, fallback);
    }

    @Override
    public Supplier<String> stringAt(String path, String fallback) {
        return config.stringAt(path, fallback);
    }

    @Override
    public Supplier<ConfigObject> objectAt(String path) {
        return config.objectAt(path);
    }

    @Override
    public Supplier<Config> configAt(String path) {
        return config.configAt(path);
    }

    @Override
    public <T> Supplier<ImmutableList<T>> listAt(String path, Class<T> type) {
        return config.listAt(path, type);
    }

    @Override
    public <T> Supplier<ImmutableMap<String, T>> mapAt(String path, Class<T> type) {
        return config.mapAt(path, type);
    }

    @Override
    public ConfigObject toObject() {
        return config.toObject();
    }

    public static YamlConfig load(File file) {
        YamlConfig config = new YamlConfig(file);
        config.load();
        return config;
    }

    private static class CustomConstructor extends SafeConstructor {
        @Override
        protected Map<Object, Object> createDefaultMap() {
            return new ConfigObject();
        }

        @Override
        protected List<Object> createDefaultList(int initSize) {
            return new ConfigList<>();
        }
    }

    private static class FancyRepresenter extends Representer {
        private FancyRepresenter() {
            this.nullRepresenter = o -> this.representScalar(Tag.NULL, "");
        }
    }

    private class FancyDumperOptions extends DumperOptions {
        private FancyDumperOptions() {
        }

        @Override
        @SuppressWarnings("deprecation")
        public ScalarStyle calculateScalarStyle(ScalarAnalysis analysis, ScalarStyle style) {
            return (analysis.scalar.contains("\n") || analysis.scalar.contains("\r"))
                    ? ScalarStyle.LITERAL
                    : super.calculateScalarStyle(analysis, style);
        }
    }

}
