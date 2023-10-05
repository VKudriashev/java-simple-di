package com.simpledi.data.thread;

import com.simpledi.data.dao.event.EventDAO;
import com.simpledi.ioc.Provider;

import java.util.concurrent.Callable;

public class TestRunnable implements Callable<EventDAO> {

    private static int taskCount = 0;
    private final int id = ++taskCount;
    private final Provider<EventDAO> daoProvider;

    public TestRunnable(Provider<EventDAO> daoProvider) {
        this.daoProvider = daoProvider;
    }

    @Override
    public EventDAO call() {
        System.out.println("Thread(" + id + ") start performed by " + Thread.currentThread().getName());
        EventDAO instance = daoProvider.getInstance();
        System.out.println("Thread(" + id + ") instance: " + instance);
        System.out.println("Thread(" + id + ") end");
        return instance;
    }
}
