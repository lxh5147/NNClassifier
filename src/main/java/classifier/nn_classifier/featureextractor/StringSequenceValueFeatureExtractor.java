package classifier.nn_classifier.featureextractor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import com.google.common.base.Joiner;

import classifier.Feature;
import classifier.FeatureExtractor;
import classifier.nn_classifier.NNClassifier;

public class StringSequenceValueFeatureExtractor implements FeatureExtractor<List<String>, Feature> {
    protected final String featureTypeName;
    protected final boolean toLowerCase;

    public StringSequenceValueFeatureExtractor(String featureTypeName,
            boolean toLowerCase) {
        this.featureTypeName = checkNotNull(featureTypeName, "featureTypeName");
        this.toLowerCase = toLowerCase;
    }

    protected static final Joiner JOINER = Joiner
            .on(NNClassifier.VALUE_SEPERATOR);

    @Override
    public Feature extract(List<String> values) {
        checkNotNull(values, "values");
        checkArgument(!values.isEmpty(), "values");
        String value = JOINER.join(values);
        return new Feature().withTypeName(this.featureTypeName).withValue(toLowerCase ? value.toLowerCase() : value);
    }

}
