package classifier;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

public class Prediction {

    public String getLabel(){
        return this.label;
    }

    public double getConfidence(){
        return this.confidence;
    }

    public Prediction withLabel(String label){
        this.label=checkNotNull(label,"label");
        return this;
    }

    public Prediction withConfidence(double confidence){
        this.confidence=confidence;
        return this;
    }

    @Override
    public String toString(){
        return Objects.toStringHelper(this.getClass().getName())
                .add("label",label)
                .add("confidence",confidence)
                .toString();
    }

    private String label;
    private double confidence;

}