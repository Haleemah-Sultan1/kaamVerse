package kaamverse.bl.model;

import java.util.ArrayList;
import java.util.List;

/** An area that contains streets. */
public class Locality {

    private int localityId;
    private String name;
    private List<Street> streets = new ArrayList<>();

    public int getLocalityId() {
        return localityId;
    }

    public void setLocalityId(int localityId) {
        this.localityId = localityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Street> getStreets() {
        return streets;
    }

    public void setStreets(List<Street> streets) {
        this.streets = streets;
    }
}
