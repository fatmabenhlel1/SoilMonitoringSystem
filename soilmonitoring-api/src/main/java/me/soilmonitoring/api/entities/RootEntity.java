package me.soilmonitoring.api.entities;

import java.io.Serializable;


// TODO: add documentation


public interface RootEntity<ID extends Serializable> extends Serializable {
    ID getId();
    void setId(ID id);
    long getVersion();
    void setVersion(long version);
}
