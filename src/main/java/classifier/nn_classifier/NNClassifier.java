package classifier.nn_classifier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nn_runtime_jni.DoubleVector;
import nn_runtime_jni.IdVector;
import nn_runtime_jni.IdsVector;
import nn_runtime_jni.nn_runtime;
import classifier.Classifier;
import classifier.Feature;
import classifier.Prediction;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class NNClassifier implements Classifier {

    @Override
    public Iterable<Prediction> predict(List<? extends Feature> features) {
        checkNotNull(features, "features");
        checkArgument(features.size()==this.symbolToIdLookupList.size(),"featurs");
        IdsVector idsVector=getIdsVector(features, this.symbolToIdLookupList);
        DoubleVector doubleVector=nn_runtime.predict(modelHandle, idsVector);
        return getPrediction(doubleVector,this.predictionIdToLabelLookup);
    }

    private static Iterable<Prediction> getPrediction( DoubleVector doubleVector, IdToSymbolLookup predictionIdToLabelLookup){
        checkNotNull(doubleVector,"doubleVector");
        checkArgument(doubleVector.size()==predictionIdToLabelLookup.getSize(),"doubleVector");
        List<Prediction> predictions=Lists.newArrayList();
        for(int i=0;i<doubleVector.size();++i){
            predictions.add(new Prediction().withLabel(predictionIdToLabelLookup.getSymbol(i)).withConfidence(doubleVector.get(i)));
        }
        return predictions;
    }
    private static IdsVector getIdsVector(List<? extends Feature> features, List<? extends SymbolToIdLookup> symbolToIdLookupList){
        IdsVector idsVector = new IdsVector();
        for(int i=0;i<features.size();++i){
            idsVector.add(getIdVector(features.get(i),symbolToIdLookupList.get(i)));
        }
        return idsVector;
    }

    private static IdVector getIdVector(Feature feature,SymbolToIdLookup lookup){
       IdVector idVector = new IdVector();
       for(String value: Splitter.on(',').split(feature.getValue())){
           idVector.add(lookup.getId(value));
       }
       return idVector;
    }

    public static NNClassifier load(File modelPath, List<File> symbolToIdLookupFiles, File predictionIdToLabelLookupFile)
            throws IOException {
        checkNotNull(modelPath, "modelPath");
        checkNotNull(symbolToIdLookupFiles, "symbolToIdLookupFiles");
        checkNotNull(predictionIdToLabelLookupFile, "predictionIdToLabelLookupFile");
        long modelHandle = nn_runtime.load(modelPath.getCanonicalPath());
        checkState(modelHandle > 0, "modelHandle");
        List<SymbolToIdLookup> symbolToIdLookupList = Lists.newArrayList();
        for (File file : symbolToIdLookupFiles) {
            symbolToIdLookupList.add(SymbolToIdLookup.load(file));
        }
        IdToSymbolLookup predictionIdToLabelLookup = IdToSymbolLookup.load(predictionIdToLabelLookupFile);
        return new NNClassifier(modelHandle, symbolToIdLookupList, predictionIdToLabelLookup);
    }

    private NNClassifier(long modelHandle, List<SymbolToIdLookup> symbolToIdLookupList,
            IdToSymbolLookup predictionIdToLabelLookup) {
        this.modelHandle = modelHandle;
        this.symbolToIdLookupList = symbolToIdLookupList;
        this.predictionIdToLabelLookup = predictionIdToLabelLookup;
    }

    private final long modelHandle;
    private final List<SymbolToIdLookup> symbolToIdLookupList;
    private final IdToSymbolLookup predictionIdToLabelLookup;

}
