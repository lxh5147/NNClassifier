package classifier;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

public class Feature {

    public double getConfidence(){
        return this.confidence;
    }

    public String getTypeName(){
        return this.typeName;
    }

    public String getValue(){
        return this.value;
    }

    public Feature withValue(String value){
        this.value=checkNotNull(value,"value");
        return this;
    }

    public Feature withConfidence(double confidence){
        this.confidence=confidence;
        return this;
    }

    public Feature withTypeName(String typeName){
        this.typeName=checkNotNull(typeName,"typeName");
        return this;
    }

    @Override
    public String toString(){
        return Objects.toStringHelper(this.getClass())
                .add("typeName", typeName)
                .add("value", value)
                .add("confidence", confidence).toString();
    }

    private double confidence;
    private String typeName;
    private String value;

}
