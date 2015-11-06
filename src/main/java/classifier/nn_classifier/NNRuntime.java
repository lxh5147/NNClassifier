package classifier.nn_classifier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import nn_runtime_jni.DoubleVector;
import nn_runtime_jni.IdVector;
import nn_runtime_jni.IdsVector;
import nn_runtime_jni.nn_runtime;

/**
 * Defines the wrapper of NN jni runtime that maps input symbols to ids.
 *
 * @author xiaohua_liu
 *
 */
public class NNRuntime {
    public List<Double> predict(final List<List<String>> features) {
        checkNotNull(features, "features");
        checkArgument(features.size() == this.symbolToIdLookupList.size(),"featurs");
        IdsVector idsVector = getIdsVector(features, this.symbolToIdLookupList);
        DoubleVector doubleVector = nn_runtime.predict(modelHandle, idsVector);
        List<Double> predictions = Lists.newArrayList();
        for (int i = 0; i < doubleVector.size(); ++i) {
            predictions.add(doubleVector.get(i));
        }
        return predictions;
    }

    public SymbolToIdLookup getSymbolToIdLookup(int index){
        checkArgument(index>=0 && index<this.symbolToIdLookupList.size(),"index");
        return this.symbolToIdLookupList.get(index);
    }

   private static IdsVector getIdsVector(List<List<String>> features, List<? extends SymbolToIdLookup> symbolToIdLookupList) {
        IdsVector idsVector = new IdsVector();
        for (int i = 0; i < features.size(); ++i) {
            idsVector.add(getIdVector(features.get(i), symbolToIdLookupList.get(i)));
        }
        return idsVector;
    }

    private static IdVector getIdVector(List<String> feature,SymbolToIdLookup lookup) {
        IdVector idVector = new IdVector();
        int[] ids=new int[feature.size()];
        int i=0;
        for (String value : feature) {
            ids[i++]=lookup.getId(value);
        }
        for(int id:ids){
            idVector.add(id);
        }
        return idVector;
    }

    public static NNRuntime load(File modelPath,List<File> symbolToIdLookupFiles) throws IOException {
        checkNotNull(modelPath, "modelPath");
        checkNotNull(symbolToIdLookupFiles, "symbolToIdLookupFiles");
        long modelHandle = nn_runtime.load(modelPath.getCanonicalPath());
        checkState(modelHandle > 0, "modelHandle");
        List<SymbolToIdLookup> symbolToIdLookupList = Lists.newArrayList();
        for (File file : symbolToIdLookupFiles) {
            symbolToIdLookupList.add(SymbolToIdLookup.load(file));
        }
        return new NNRuntime(modelHandle, symbolToIdLookupList);
    }

    private NNRuntime(long modelHandle, List<SymbolToIdLookup> symbolToIdLookupList) {
        this.modelHandle = modelHandle;
        this.symbolToIdLookupList = symbolToIdLookupList;
    }

    private final long modelHandle;
    private final List<SymbolToIdLookup> symbolToIdLookupList;

    static {
        System.loadLibrary("nn_runtime_jni");
    }
}
