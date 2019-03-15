package depsolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Package implements Comparable<Package> {
    private String name;
    private String version;
    private Integer size;
    private List<List<String>> depends = new ArrayList<>();
    private List<String> conflicts = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Integer getSize() {
        return size;
    }

    public List<List<String>> getDepends() {
        return depends;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setDepends(List<List<String>> depends) {
        this.depends = depends;
    }

    public void setConflicts(List<String> conflicts) {
        this.conflicts = conflicts;
    }

    /**
     * Checks to see if the package version matches the expected dependency version
     * @param expectedVersion The version number that is expected
     * @param givenVersion The version that is given
     * @param comparator THe comparison for the package version
     * @return
     */
    public static boolean checkVersion(String expectedVersion, String givenVersion, String comparator){
        String [] ev = expectedVersion.split("\\.");
        String [] gv = givenVersion.split("\\.");
        boolean validVersion = true;
        int maxLoop =  ev.length < gv.length ? gv.length : ev.length;
        for(int i = 0; i < maxLoop; i++){

            int e = i < ev.length ? Integer.parseInt(ev[i]) : 0;
            int v = i < gv.length ? Integer.parseInt(gv[i]) : 0;
            switch(comparator){
                case Constants.EQUAL:
                    if(v != e)
                        validVersion = false;
                    break;
                case Constants.GREATER_OR_EQUAL:
                    if(v < e)
                        validVersion = false;
                    break;
                case Constants.LESS_OR_EQUAL:
                    if(v > e)
                        validVersion = false;
                    break;
                case Constants.GREATER_THAN:
                    if(v <= e)
                        validVersion = false;
                    break;
                case Constants.LESS_THAN:
                    if(v >= e)
                        validVersion = false;
                    break;
            }
            if(validVersion == false)
                break;
        }

        return validVersion;
    }

    public static String getComparator(String p){
        String comparator = "";
        if(p.contains(Constants.GREATER_OR_EQUAL)){
            comparator = Constants.GREATER_OR_EQUAL;
        }
        else if(p.contains(Constants.LESS_OR_EQUAL)){
            comparator = Constants.LESS_OR_EQUAL;
        } else if(p.contains(Constants.GREATER_THAN)) {
            comparator = Constants.GREATER_THAN;
        } else if(p.contains(Constants.LESS_THAN)){
            comparator = Constants.LESS_THAN;
        }  else if(p.contains(Constants.EQUAL)){
            comparator = Constants.EQUAL;
        }

        return comparator;
    }

    /**
     * Obtain package from repo with a given name and version
     * @param repo The repo list to search from
     * @param name Name of the package you are looking for
     * @param version The version of the package
     * @return The package if found else null
     */
    public static Package getPackage(List<Package> repo, String name, String version){
        for(Package p : repo){
            if(p.getName().equals(name) && p.getVersion().equals(version)){
                return p;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Package o) {
        return this.name.compareTo(o.getName());
    }
}
