package com.simpledi.data.dao.test;

import com.simpledi.data.domain.Entity;

import java.util.Collection;
import java.util.Collections;

public class InMemoryTestDao implements TestDao {

    @Override
    public Collection<Entity> getTests() {
        return Collections.emptyList();
    }
}
