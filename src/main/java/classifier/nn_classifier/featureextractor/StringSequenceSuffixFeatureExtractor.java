package classifier.nn_classifier.featureextractor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.Lists;

import classifier.Feature;

public class StringSequenceSuffixFeatureExtractor extends  StringSequenceValueFeatureExtractor{

    private final int suffixLength;

    public StringSequenceSuffixFeatureExtractor(String featureTypeName,boolean toLowerCase, int suffixLength) {
        super(featureTypeName, toLowerCase);
        checkArgument(suffixLength > 0, "suffixLength");
        this.suffixLength = suffixLength;
    }

    @Override
    public Feature extract(List<String> values) {
        checkNotNull(values, "values");
        checkArgument(!values.isEmpty(), "values");
        List<String> suffixes = Lists.newArrayList();
        values.forEach( value->{
            suffixes.add(this.getSuffix(value));
        });
        String value = JOINER.join(suffixes);
        return new Feature().withTypeName(this.featureTypeName).withValue(toLowerCase ? value.toLowerCase() : value);
    }

    private String getSuffix(String value) {
        return this.suffixLength < value.length() ? value.substring( value.length()-this.suffixLength) : value;
    }

}
