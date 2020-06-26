package com.java.learning.classifier;

public interface FeatureSelectionProb<T, K> {
    public float featureProbability(T feature, K category);
}
