package com.simpledi.data.service;

import java.util.TreeSet;

public class DefaultConstructorFindPreMaxService {

    private final int[] myArray = {5, 7, 7, 6, 6, 6, 9, 9, 9, 6, 6, 6, 6, 6, 8, 8};

    private Integer count;

    public DefaultConstructorFindPreMaxService() {

    }

    public DefaultConstructorFindPreMaxService(Integer count) {
        this.count = count;
    }

    private DefaultConstructorFindPreMaxService(String count) {
        this.count = Integer.valueOf(count);
    }

    /**
     * Находит второй максимальный элемент массива 2 способами
     */
    public void execute() {
        System.out.println("DefaultConstructorFindPreMaxService execute()");
        long before = System.currentTimeMillis();
        System.out.println("Second max value: " + findPreMaxTreeSet(myArray));
        System.out.println("findPreMaxTreeSet time: " + (System.currentTimeMillis() - before) + " ms");

        before = System.currentTimeMillis();
        System.out.println("Second max value: " + findPreMax(myArray));
        System.out.println("findPreMax time: " + (System.currentTimeMillis() - before) + " ms");
    }

    private Integer findPreMaxTreeSet(int[] array) {
        TreeSet<Integer> treeSet = new TreeSet<>();
        // Arrays.stream(arr).forEach(treeSet::add); // это более долгая операция
        for (int element : array) {
            treeSet.add(element);
        }
        return treeSet.lower(treeSet.last());
    }

    private Integer findPreMax(int[] array) {
        int max = 0, secondMax = 0;
        for (int element : array) {
            if (element > max) {
                secondMax = max;
                max = element;
            } else if (element > secondMax && element < max) {
                secondMax = element;
            }
        }
        return secondMax;
    }

}
