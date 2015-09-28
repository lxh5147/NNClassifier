package classifier.nn_classifier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SymbolToIdLookup {
    public static final long UNK = 0l;

    public long getId(String symbol) {
        checkNotNull(symbol, "symbol");
        return this.symbolToIdMap.containsKey(symbol) ? this.symbolToIdMap.get(symbol) : UNK;
    }

    public static SymbolToIdLookup load(File file) throws IOException {
        checkNotNull(file, "file");
        String symbolToIdLookupKey = file.getCanonicalPath();
        if (SYMBOL_TO_ID_LOOKUP_TABLES.containsKey(symbolToIdLookupKey)) {
            return SYMBOL_TO_ID_LOOKUP_TABLES.get(symbolToIdLookupKey);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))) {
            String line = null;
            Map<String, Long> symbolToIdMap = Maps.newHashMap();
            Splitter splitter = Splitter.on('\t');
            while ((line = reader.readLine()) != null) {
                List<String> parts = Lists.newArrayList(splitter.split(line));
                checkArgument(parts.size() == 2, "line");
                symbolToIdMap.put(parts.get(0), Long.valueOf(parts.get(1)));
            }
            SymbolToIdLookup symbolToIdLookup = new SymbolToIdLookup(symbolToIdMap);
            SYMBOL_TO_ID_LOOKUP_TABLES.put(symbolToIdLookupKey, symbolToIdLookup);
            return symbolToIdLookup;
        }
    }

    private SymbolToIdLookup(Map<String, Long> symbolToIdMap) {
        this.symbolToIdMap = Maps.newHashMap(checkNotNull(symbolToIdMap, "symbolToIdMap"));
    }

    private final Map<String, Long> symbolToIdMap;

    private static Map<String, SymbolToIdLookup> SYMBOL_TO_ID_LOOKUP_TABLES = Maps.newConcurrentMap();

}
