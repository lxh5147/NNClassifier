package classifier;

import java.util.List;

public interface Classifier {
    Iterable<? extends Prediction> predict(List<? extends Feature> features);
}
