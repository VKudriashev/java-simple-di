package com.simpledi.data.dao.event;

import com.simpledi.data.dao.test.TestDao;
import com.simpledi.data.domain.Entity;

import java.util.Collection;
import java.util.Collections;

public class InMemoryEventDAO implements EventDAO {

    private TestDao testDao;

    public InMemoryEventDAO(TestDao testDao) {
        this.testDao = testDao;
    }

    public InMemoryEventDAO() {
        // Этот код эмулирует медленную инициализацию.
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Collection<Entity> getEvents() {
        return Collections.emptyList();
    }
}
