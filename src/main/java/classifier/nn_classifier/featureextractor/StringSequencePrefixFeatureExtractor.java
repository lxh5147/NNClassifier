package classifier.nn_classifier.featureextractor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.Lists;

import classifier.Feature;

public class StringSequencePrefixFeatureExtractor extends  StringSequenceValueFeatureExtractor{

    private final int prefixLength;

    public StringSequencePrefixFeatureExtractor(String featureTypeName,boolean toLowerCase, int prefixLength) {
        super(featureTypeName, toLowerCase);
        checkArgument(prefixLength > 0, "prefixLength");
        this.prefixLength = prefixLength;
    }

    @Override
    public Feature extract(List<String> values) {
        checkNotNull(values, "values");
        checkArgument(!values.isEmpty(), "values");
        List<String> prefixes = Lists.newArrayList();
        values.forEach( value->{
            prefixes.add(this.getPrefix(value));
        });
        String value = JOINER.join(prefixes);
        return new Feature().withTypeName(this.featureTypeName).withValue(toLowerCase ? value.toLowerCase() : value);
    }

    private String getPrefix(String value) {
        return this.prefixLength < value.length() ? value.substring(0,
                this.prefixLength) : value;
    }

}
