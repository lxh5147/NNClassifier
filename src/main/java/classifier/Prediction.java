package classifier;

public interface Prediction {
    String getLabel();
    double getConfidence();
}