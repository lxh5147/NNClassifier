package classifier;

public interface FeatureExtractor<T,V extends Feature> {
    V extract(T t);
}
