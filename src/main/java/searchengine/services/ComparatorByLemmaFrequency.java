package searchengine.services;

import searchengine.model.Lemma;

import java.util.Comparator;

public class ComparatorByLemmaFrequency implements Comparator<Lemma> {

    @Override
    public int compare(Lemma l1, Lemma l2) {
        return Double.compare(l1.getFrequency(), l2.getFrequency());
    }
}
