package me.soilmonitoring.iam.entities;

import java.io.Serializable;

public interface RootEntity<ID extends Serializable> extends Serializable {
    ID getId();
    void setId(ID id);
    long getVersion();
    void setVersion(long version);
}
