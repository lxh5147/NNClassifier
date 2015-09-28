package classifier.combo;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import classifier.Classifier;
import classifier.Feature;
import classifier.Prediction;

import com.google.common.collect.Maps;

public class ComboClassifier implements Classifier {

    @Override
    public Iterable<Prediction> predict(final List<? extends Feature> features) {
        Map<String, Prediction> labelToPredictionMap = Maps.newHashMap();
        for(int i=0;i<this.classifiers.length;++i){
            double weight=this.weights[i];
            if(weight>0){
                for(Prediction prediction: this.classifiers[i].predict(features)){
                    String label=prediction.getLabel();
                    Prediction combinedPrediction=labelToPredictionMap.get(label);
                    if(combinedPrediction==null){
                        combinedPrediction=new Prediction().withLabel(label).withConfidence(0d);
                        labelToPredictionMap.put(label, combinedPrediction);
                    }
                    combinedPrediction.withConfidence(combinedPrediction.getConfidence()+prediction.getConfidence()*weight);
                }
            }
        }
        return labelToPredictionMap.values();
    }

    public ComboClassifier(Classifier[] classifiers, double[] weights) {
        checkNotNull(classifiers, "classifiers");
        checkNotNull(weights, "weights");
        checkArgument(classifiers.length > 0, "classifiers");
        checkArgument(weights.length == classifiers.length, "weights");
        this.classifiers = classifiers;
        // warning: input weights may be updated by the normalization
        this.weights = normalizeWeights(weights);
    }

    private static double[] normalizeWeights(double[] weights) {
        double total = 0d;
        for (int i = 0; i < weights.length; ++i) {
            total += weights[i];
        }
        checkArgument(total > 0, "total");
        for (int i = 0; i < weights.length; ++i) {
            weights[i] /= total;
        }
        return weights;
    }

    private final Classifier[] classifiers;
    private final double[] weights;

}
