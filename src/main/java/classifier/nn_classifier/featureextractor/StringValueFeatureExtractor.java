package classifier.nn_classifier.featureextractor;

import static com.google.common.base.Preconditions.checkNotNull;
import classifier.Feature;
import classifier.FeatureExtractor;
/**
 * Extracts string value as feature, e.g., field id as non sequence feature.
 * @author lxh5147
 *
 */
public class StringValueFeatureExtractor implements FeatureExtractor<String, Feature>{
    private final String featureTypeName;
    private final boolean toLowerCase;

    public StringValueFeatureExtractor(String featureTypeName,boolean toLowerCase) {
        this.featureTypeName = checkNotNull(featureTypeName, "featureTypeName");
        this.toLowerCase=toLowerCase;
    }

    @Override
    public Feature extract(String value) {
        checkNotNull(value, "value");
        return new Feature().withTypeName(this.featureTypeName).withValue(toLowerCase? value.toLowerCase():value);
    }

}
