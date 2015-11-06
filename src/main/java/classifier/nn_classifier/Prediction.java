package classifier.nn_classifier;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

public class Prediction {

    public String getLabel() {
        return this.label;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public Prediction withLabel(String label) {
        this.label = checkNotNull(label, "label");
        return this;
    }

    public Prediction withConfidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass().getName())
                .add("label", label).add("confidence", confidence).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Prediction other = (Prediction) obj;
        return Objects.equal(this.label, other.label)
            && equals(this.confidence,other.confidence);
    }

    private static boolean equals(double d1, double d2){
        return Math.abs(d1-d2)<0.000001;
    }

    private String label;
    private double confidence;

}