package depsolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SatPackage {
    private String packageName;
    private String packageVersion;
    private List<HashSet<SatPackage>> dependencies;
    private List<String> conflicts;
    private HashSet<SatPackage> dependents;

    public SatPackage(String packageName, String packageVersion) {
        this.packageName = packageName;
        this.packageVersion = packageVersion;
        dependencies = new ArrayList<>();
        conflicts = new ArrayList<>();
        dependents = new HashSet<>();
    }

    public void setDependenciesAndConflicts(List<SatPackage> finalState, List<Package> repo) {
        Package p = Package.getPackage(repo, packageName, packageVersion);
        this.conflicts = p.getConflicts();
        for (List<String> deps : p.getDepends()) {
            HashSet<SatPackage> innerD = new HashSet<>();
            for (String innerDeps : deps) {
                for (SatPackage sat : finalState) {
                    String comparator = Package.getComparator(innerDeps);
                    String[] expectedPackage = innerDeps.split(comparator);
                    if (comparator.equals("")) {
                        if (sat.packageName.equals(innerDeps)) {
                            innerD.add(sat);
                            break;
                        }
                    } else if (sat.packageName.equals(expectedPackage[0]) && Package.checkVersion(expectedPackage[1], sat.packageVersion, comparator)) {
                        innerD.add(sat);
                        break;
                    }
                }
                if (!innerD.isEmpty()) {
                    break;
                }
            }
            this.dependencies.add(innerD);
        }
    }

    public void setDependencyAndConflictsInitial(List<SatPackage> finalState, List<Package> repo) {
        Package p = Package.getPackage(repo, packageName, packageVersion);
        this.conflicts = p.getConflicts();
        for (List<String> deps : p.getDepends()) {
            HashSet<SatPackage> innerD = new HashSet<>();
            for (String innerDeps : deps) {
                for (SatPackage sat : finalState) {
                    String comparator = Package.getComparator(innerDeps);
                    String[] expectedPackage = innerDeps.split(comparator);
                    if (comparator.equals("")) {
                        if (sat.packageName.equals(innerDeps)) {
                            innerD.add(sat);
                            sat.getDependents().add(this);
                        }
                    } else if (sat.packageName.equals(expectedPackage[0]) && Package.checkVersion(expectedPackage[1], sat.packageVersion, comparator)) {
                        innerD.add(sat);
                        sat.getDependents().add(this);
                    }
                }
            }
            this.dependencies.add(innerD);
        }
    }


    public String getPackageName() {
        return packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    public HashSet getDependents() {
        return dependents;
    }

    public List<HashSet<SatPackage>> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        String toPrint = "Package Name: " + this.packageName + " Package Version: " + this.packageVersion + " Dependencies: {\n";
        for (HashSet<SatPackage> spd : dependencies) {
            for (SatPackage sat : spd) {
                String temp = "\t Package Name: " + sat.getPackageName() + " Version: " + sat.getPackageVersion() + "\n";
                toPrint = toPrint + temp;
            }
        }
        toPrint = toPrint + "}";
        return toPrint;
    }
}