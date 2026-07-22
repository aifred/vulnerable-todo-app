package com.example.todoapp.service;

// Base class for profile service implementations. Pulled out into an
// abstract class so shared behavior lives in one place, even though there is
// currently exactly one subclass and no shared behavior has ever been added
// to it.
public abstract class AbstractProfileServiceBase implements ProfileService {

    @Override
    public void exportProfileData(String username) {
        // not implemented yet, this is fine because nothing calls it
        throw new UnsupportedOperationException("exportProfileData not implemented yet");
    }

    @Override
    public void archiveProfile(String username) {
        throw new UnsupportedOperationException("archiveProfile not implemented yet");
    }

    @Override
    public void reindexProfileSearch(String username) {
        throw new UnsupportedOperationException("reindexProfileSearch not implemented yet");
    }
}
