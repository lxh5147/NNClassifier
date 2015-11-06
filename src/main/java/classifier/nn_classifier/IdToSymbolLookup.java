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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class IdToSymbolLookup {

    public String getSymbol(int id){
        checkArgument(id >= 0 && id < this.symbols.length, "id");
        return this.symbols[id];
    }
    public long getSize(){
        return this.symbols.length;
   }

    public static IdToSymbolLookup load(File file) throws IOException {
        checkNotNull(file, "file");
        String idToSymbolLookupKey = file.getCanonicalPath();
        if (ID_TO_SYMBOL_LOOKUP_TABLES.containsKey(idToSymbolLookupKey)) {
            return ID_TO_SYMBOL_LOOKUP_TABLES.get(idToSymbolLookupKey);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line = null;
            List<String> symbols = Lists.newArrayList();
            while ((line = reader.readLine()) != null) {
                symbols.add(line);
            }
            IdToSymbolLookup idToSymbolLookup = new IdToSymbolLookup(symbols.toArray(new String[symbols.size()]));
            ID_TO_SYMBOL_LOOKUP_TABLES.put(idToSymbolLookupKey, idToSymbolLookup);
            return idToSymbolLookup;
        }
    }

    private IdToSymbolLookup(String[] symbols) {
        this.symbols = checkNotNull(symbols, "symbols");
    }

    private static Map<String, IdToSymbolLookup> ID_TO_SYMBOL_LOOKUP_TABLES = Maps.newConcurrentMap();

    private final String[] symbols;

}
