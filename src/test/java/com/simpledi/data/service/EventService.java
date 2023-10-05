package com.simpledi.data.service;

import com.simpledi.data.dao.event.EventDAO;
import com.simpledi.data.dao.profile.ProfileDAO;

import javax.inject.Inject;

public class EventService {

    private final EventDAO dao;
    private final ProfileDAO profileDAO;

    @Inject
    public EventService(EventDAO dao, ProfileDAO profileDAO) {
        this.dao = dao;
        this.profileDAO = profileDAO;
    }
}
