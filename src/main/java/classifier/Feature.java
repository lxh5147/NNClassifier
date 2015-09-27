package classifier;

public interface Feature {
    double getConfidence();
    String getTypeName();
    String getValue();
}
