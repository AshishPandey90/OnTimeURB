package com.java.learning.classifier;

import java.util.*;

public abstract class GenericClassifier<T, K> implements FeatureSelectionProb<T, K> {

    private static final int INITIAL_CATEGORY_DICTIONARY_CAPACITY = 16;
    private static final int INITIAL_FEATURE_DICTIONARY_CAPACITY = 32;
    private int memoryCapacity = 1000;
    private Dictionary<K, Dictionary<T, Integer>> featureCountPerCategory;
    private Dictionary<T, Integer> totalFeatures;
    private Dictionary<K, Integer> allCategories;
    private Queue<ClassificationParam<T, K>> memoryQueue;
    public GenericClassifier() {
        this.reset();
    }

    public void reset() {
        this.featureCountPerCategory = new Hashtable<K, Dictionary<T, Integer>>(
                GenericClassifier.INITIAL_CATEGORY_DICTIONARY_CAPACITY);
        this.totalFeatures = new Hashtable<T, Integer>(GenericClassifier.INITIAL_FEATURE_DICTIONARY_CAPACITY);
        this.allCategories = new Hashtable<K, Integer>(GenericClassifier.INITIAL_CATEGORY_DICTIONARY_CAPACITY);
        this.memoryQueue = new LinkedList<ClassificationParam<T, K>>();
    }

    public Set<T> getFeatures() {
        return ((Hashtable<T, Integer>) this.totalFeatures).keySet();
    }

    public Set<K> getCategories() {
        return ((Hashtable<K, Integer>) this.allCategories).keySet();
    }

    public int getCategoriesTotal() {
        int toReturn = 0;
        for (Enumeration<Integer> e = this.allCategories.elements(); e.hasMoreElements();) {
            toReturn += e.nextElement();
        }
        return toReturn;
    }

    public int getMemoryCapacity() {
        return memoryCapacity;
    }

    public void setMemoryCapacity(int memoryCapacity) {
        for (int i = this.memoryCapacity; i > memoryCapacity; i--) {
            this.memoryQueue.poll();
        }
        this.memoryCapacity = memoryCapacity;
    }

    public void incrementFeature(T feature, K category) {
        Dictionary<T, Integer> features = this.featureCountPerCategory.get(category);
        if (features == null) {
            this.featureCountPerCategory.put(category,
                    new Hashtable<T, Integer>(GenericClassifier.INITIAL_FEATURE_DICTIONARY_CAPACITY));
            features = this.featureCountPerCategory.get(category);
        }
        Integer count = features.get(feature);
        if (count == null) {
            features.put(feature, 0);
            count = features.get(feature);
        }
        features.put(feature, ++count);

        Integer totalCount = this.totalFeatures.get(feature);
        if (totalCount == null) {
            this.totalFeatures.put(feature, 0);
            totalCount = this.totalFeatures.get(feature);
        }
        this.totalFeatures.put(feature, ++totalCount);
    }

    public void incrementCategory(K category) {
        Integer count = this.allCategories.get(category);
        if (count == null) {
            this.allCategories.put(category, 0);
            count = this.allCategories.get(category);
        }
        this.allCategories.put(category, ++count);
    }

    public void decrementFeature(T feature, K category) {
        Dictionary<T, Integer> features = this.featureCountPerCategory.get(category);
        if (features == null) {
            return;
        }
        Integer count = features.get(feature);
        if (count == null) {
            return;
        }
        if (count.intValue() == 1) {
            features.remove(feature);
            if (features.size() == 0) {
                this.featureCountPerCategory.remove(category);
            }
        } else {
            features.put(feature, --count);
        }

        Integer totalCount = this.totalFeatures.get(feature);
        if (totalCount == null) {
            return;
        }
        if (totalCount.intValue() == 1) {
            this.totalFeatures.remove(feature);
        } else {
            this.totalFeatures.put(feature, --totalCount);
        }
    }

    public void decrementCategory(K category) {
        Integer count = this.allCategories.get(category);
        if (count == null) {
            return;
        }
        if (count.intValue() == 1) {
            this.allCategories.remove(category);
        } else {
            this.allCategories.put(category, --count);
        }
    }

    public int getFeatureCount(T feature, K category) {
        Dictionary<T, Integer> features = this.featureCountPerCategory.get(category);
        if (features == null) return 0;
        Integer count = features.get(feature);
        return (count == null) ? 0 : count.intValue();
    }

    public int getFeatureCount(T feature) {
        Integer count = this.totalFeatures.get(feature);
        return (count == null) ? 0 : count.intValue();
    }

    public int getCategoryCount(K category) {
        Integer count = this.allCategories.get(category);
        return (count == null) ? 0 : count.intValue();
    }

    public float featureProbability(T feature, K category) {
        final float totalFeatureCount = this.getFeatureCount(feature);

        if (totalFeatureCount == 0) {
            return 0;
        } else {
            return this.getFeatureCount(feature, category) / (float) this.getFeatureCount(feature);
        }
    }


    public float featureWeighedAverage(T feature, K category) {
        return this.featureWeighedAverage(feature, category, null, 1.0f, 0.5f);
    }

    public float featureWeighedAverage(T feature, K category, FeatureSelectionProb<T, K> calculator) {
        return this.featureWeighedAverage(feature, category, calculator, 1.0f, 0.5f);
    }

    public float featureWeighedAverage(T feature, K category, FeatureSelectionProb<T, K> calculator, float weight) {
        return this.featureWeighedAverage(feature, category, calculator, weight, 0.5f);
    }

    public float featureWeighedAverage(T feature, K category, FeatureSelectionProb<T, K> calculator, float weight,
            float assumedProbability) {

        final float basicProbability = (calculator == null) ? this.featureProbability(feature, category)
                : calculator.featureProbability(feature, category);

        Integer totals = this.totalFeatures.get(feature);
        if (totals == null) totals = 0;
        return (weight * assumedProbability + totals * basicProbability) / (weight + totals);
    }

    public void learn(K category, Collection<T> features) {
        this.learn(new ClassificationParam<T, K>(features, category));
    }

    public void learn(ClassificationParam<T, K> classification) {

        for (T feature : classification.getFeatureset())
            this.incrementFeature(feature, classification.getCategory());
        this.incrementCategory(classification.getCategory());

        this.memoryQueue.offer(classification);
        if (this.memoryQueue.size() > this.memoryCapacity) {
            ClassificationParam<T, K> toForget = this.memoryQueue.remove();

            for (T feature : toForget.getFeatureset())
                this.decrementFeature(feature, toForget.getCategory());
            this.decrementCategory(toForget.getCategory());
        }
    }

    public abstract ClassificationParam<T, K> classify(Collection<T> features);

}
