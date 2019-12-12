package com.java.learning.classifier;

import java.util.*;

public class NaiveBayes<T, K> extends GenericClassifier<T, K> {

    private float featuresProbabilityProduct(Collection<T> features, K category) {
        float product = 1.0f;
        for (T feature : features)
            product *= this.featureWeighedAverage(feature, category);
        return product;
    }

    private float categoryProbability(Collection<T> features, K category) {
        return ((float) this.getCategoryCount(category)
                    / (float) this.getCategoriesTotal())
                * featuresProbabilityProduct(features, category);
    }

    private SortedSet<ClassificationParam<T, K>> categoryProbabilities(Collection<T> features) {

        SortedSet<ClassificationParam<T, K>> probabilities =
                new TreeSet<ClassificationParam<T, K>>(
                        new Comparator<ClassificationParam<T, K>>() {

                    public int compare(ClassificationParam<T, K> o1,
                            ClassificationParam<T, K> o2) {
                        int toReturn = Float.compare(
                                o1.getProbability(), o2.getProbability());
                        if ((toReturn == 0)
                                && !o1.getCategory().equals(o2.getCategory()))
                            toReturn = -1;
                        return toReturn;
                    }
                });

        for (K category : this.getCategories())
            probabilities.add(new ClassificationParam<T, K>(
                    features, category,
                    this.categoryProbability(features, category)));
        return probabilities;
    }

    @Override
    public ClassificationParam<T, K> classify(Collection<T> features) {
        SortedSet<ClassificationParam<T, K>> probabilites =this.categoryProbabilities(features);
        if (probabilites.size() > 0) {
            return probabilites.last();
        }
        return null;
    }

    public Collection<ClassificationParam<T, K>> classifyDetailed(
            Collection<T> features) {
        return this.categoryProbabilities(features);
    }
}
