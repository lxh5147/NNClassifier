package classifier.nn_classifier;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Defines NN classifier that maps output id to output label.
 *
 * @author xiaohua_liu
 *
 */
public class NNClassifier {

    public Iterable<Prediction> predict(final List<List<String>> features) {
        checkNotNull(features, "features");
        return getPrediction( this.nn.predict(features), this.predictionIdToLabelLookup);
    }

    public SymbolToIdLookup getSymbolToIdLookup(int index){
        return this.nn.getSymbolToIdLookup(index);
    }

    private static Iterable<Prediction> getPrediction(List<Double> doubleVector,IdToSymbolLookup predictionIdToLabelLookup) {
        checkNotNull(doubleVector, "doubleVector");
        checkArgument(doubleVector.size() == predictionIdToLabelLookup.getSize(),"doubleVector");
        List<Prediction> predictions = Lists.newArrayList();
        for (int i = 0; i < doubleVector.size(); ++i) {
            predictions.add(new Prediction()
                    .withLabel(predictionIdToLabelLookup.getSymbol(i))
                    .withConfidence(doubleVector.get(i)));
        }
        return predictions;
    }

    public static NNClassifier load(File modelPath,List<File> symbolToIdLookupFiles,File predictionIdToLabelLookupFile) throws IOException {
        checkNotNull(modelPath, "modelPath");
        checkNotNull(symbolToIdLookupFiles, "symbolToIdLookupFiles");
        checkNotNull(predictionIdToLabelLookupFile,"predictionIdToLabelLookupFile");
        IdToSymbolLookup predictionIdToLabelLookup = IdToSymbolLookup.load(predictionIdToLabelLookupFile);
        return new NNClassifier(modelPath, symbolToIdLookupFiles, predictionIdToLabelLookup);
    }

    private NNClassifier(File modelPath, List<File> symbolToIdLookupFiles, IdToSymbolLookup predictionIdToLabelLookup) throws IOException {
        this.nn=NNRuntime.load(modelPath, symbolToIdLookupFiles);
        this.predictionIdToLabelLookup = predictionIdToLabelLookup;
    }

    private final NNRuntime nn;

    private final IdToSymbolLookup predictionIdToLabelLookup;
}
