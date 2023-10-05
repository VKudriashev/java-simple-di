package com.simpledi.data.dao.event;

import com.simpledi.data.domain.Entity;

import java.util.Collection;

public interface EventDAO {

    Collection<Entity> getEvents();
}
