package classifier;

import java.util.List;

public interface Classifier {
    Iterable<Prediction> predict(final List<? extends Feature> features);
}
