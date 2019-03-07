package depsolver;

import java.util.ArrayList;
import java.util.List;

public class SatPackage {
    private String packageName;
    private String packageVersion;
    private List<SatPackage> dependencies;
    private List<String> conflicts;

    public SatPackage(String packageName, String packageVersion) {
        this.packageName = packageName;
        this.packageVersion = packageVersion;
        dependencies = new ArrayList<>();
        conflicts = new ArrayList<>();
    }

    public void setDependenciesAndConflicts(List<SatPackage> finalState, List<Package> repo) {
        Package p = Package.getPackage(repo, packageName, packageVersion);
        this.conflicts = p.getConflicts();
        for (List<String> deps : p.getDepends()) {
            SatPackage d = null;
            for (String innerDeps : deps) {
                for (SatPackage sat : finalState) {
                    String comparator = Package.getComparator(innerDeps);
                    String[] expectedPackage = innerDeps.split(comparator);
                    if(comparator.equals("")) {
                        if (sat.packageName.equals(innerDeps)) {
                            d = sat;
                            break;
                        }
                    }
                    else if (sat.packageName.equals(expectedPackage[0]) && Package.checkVersion(expectedPackage[1], sat.packageVersion, comparator)) {
                        d = sat;
                        break;
                    }
                }
                if(d != null){
                    break;
                }
            }
            this.dependencies.add(d);
        }
    }

    public String getPackageName(){
        return packageName;
    }

    public String getPackageVersion(){
        return packageVersion;
    }

    @Override
    public String toString(){
        String toPrint = "Package Name: " + this.packageName + " Package Version: " + this.packageVersion + " Dependencies: {\n";
        for(SatPackage sat : dependencies){
            String temp = "\t Package Name: " + sat.getPackageName() + " Version: " + sat.getPackageVersion() + "\n";
            toPrint = toPrint + temp;
        }
        toPrint = toPrint + "}";
        return toPrint;
    }
}