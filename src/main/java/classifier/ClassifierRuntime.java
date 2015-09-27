package classifier;

import java.util.List;

public interface ClassifierRuntime {
    Iterable<? extends Prediction> predict(List<? extends Feature> features);
}
