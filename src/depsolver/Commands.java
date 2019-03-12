package depsolver;

import java.util.*;

public class Commands {
    private HashMap<String, SatPackage> initial;
    private List<SatPackage> finalState;
    private Stack<String> waitingList;
    private LinkedList<String> finalCommands;
    private List<String> constraints;

    public Commands(HashMap<String, SatPackage> repo, List<SatPackage> finalState, List<String> constraints) {
        this.initial = repo;
        this.finalState = finalState;
        waitingList = new Stack<>();
        finalCommands = new LinkedList<>();
        this.constraints = constraints;
    }

    //TODO Investigate unnecessary installation
    public List<String> BuildCommandsList() {
        for (SatPackage s : finalState) {
            boolean isMissing = installDependencies(s);
            if (!isMissing ) {
                initial.put(createPackageVersionKey(s), s);
                finalCommands.add(createInstallCommand(s));
            }
        }

        constraints.forEach(c -> {
            if (c.contains("-")) {
                uninstall(c.replace("-", ""));
            }
        });

        return finalCommands;
    }

    //TODO Fix recursive call to check dependency already exists
    public boolean installDependencies(SatPackage p) {
        for (HashSet<SatPackage> s : p.getDependencies()) {
            for (SatPackage sat : s) {
                if(!sat.getDependencies().isEmpty()) {
                    installDependencies(sat);
                }
                if (!initial.containsKey(createPackageVersionKey(sat))) {
                    uninstallConflicts(sat);
                    initial.put(createPackageVersionKey(sat), sat);
                    finalCommands.add(createInstallCommand(sat));
                }
            }
        }
        if (initial.containsValue(p)) {
            return true;
        } else {
            return false;
        }
    }

    public void uninstallConflicts(SatPackage p) {
        p.getConflicts().forEach(conflict -> {
            uninstall(conflict);
        });
    }

    public void uninstall(String conflict) {
        String comparator = Package.getComparator(conflict);
        Iterator<SatPackage> ite = initial.values().iterator();
        if (comparator.equals("")) {
            while (ite.hasNext()) {
                SatPackage v = ite.next();
                if (v.getPackageName().equals(conflict)) {
                    uninstallByDependents(v);
                    waitingList.push(createPackageVersionKey(v));
                }
            }
        } else {
            String[] conflictSplit = conflict.split(comparator);
            while (ite.hasNext()) {
                SatPackage v = ite.next();
                if (v.getPackageName().equals(conflictSplit[0]) && Package.checkVersion(conflictSplit[1], v.getPackageVersion(), comparator)) {
                    uninstallByDependents(v);
                    waitingList.push(createPackageVersionKey(v));
                }
            }
        }

        while (!waitingList.empty()) {
            initial.remove(waitingList.pop());
        }
    }

    public void uninstallByDependents(SatPackage p) {
        if (p.getDependents().isEmpty()) {
            finalCommands.add(createUninstallCommand(p));
            waitingList.push(createUninstallCommand(p));
        } else {
            Iterator<SatPackage> ite = p.getDependents().iterator();
            while (ite.hasNext()) {
                SatPackage s = ite.next();
                if (checkForOnlyDependency(s, p)) {
                    uninstallByDependents(s);
                }
                finalCommands.add(createUninstallCommand(s));
                waitingList.push(createUninstallCommand(p));
            }
        }
    }

    /**
     * Checks if this is this package only relies on one dependent
     *
     * @param p   Current package
     * @param dep The package dependency
     * @return true if it is the only dependency false if not
     */
    public boolean checkForOnlyDependency(SatPackage p, SatPackage dep) {
        for (HashSet<SatPackage> sat : p.getDependencies()) {
            if (sat.contains(dep) && sat.size() > 1) {
                return true;
            }
        }
        return false;
    }

    static String createPackageVersionKey(SatPackage p) {
        return p.getPackageName() + "=" + p.getPackageVersion();
    }

    static String createInstallCommand(SatPackage p) {
        return "+" + createPackageVersionKey(p);
    }

    static String createUninstallCommand(SatPackage p) {
        return "-" + createPackageVersionKey(p);
    }
}

//    String conflictCommands = "";
//        for(String conflict : p.getConflicts()){
//                String comparator = Package.getComparator(conflict);
//                for(String installed : inital){
//                String temp = installed.replace("+", "");
//                String [] tempList = temp.split(Constants.EQUAL);
//                if(comparator.isEmpty() && conflict.equals(tempList[0])){
//                conflictCommands = createUninstallCommand(p) + "\n";
//                inital.forEach(v -> {
//                Package pack = repo.get(v.replace("+", ""));
//
//                });
//                conflictCommands = resolveConflicts(repo.get(tempList[0] + "=" + tempList[1])) + conflictCommands;
//                } else {
//                String [] conflictTemp = conflict.split(comparator);
//                if(conflictTemp[0].equals(tempList[0]) &&  Package.checkVersion(conflictTemp[1], tempList[1], comparator)){
//                conflictCommands = createUninstallCommand(p) + "\n";
//                conflictCommands = resolveConflicts(repo.get(tempList[0] + "=" + tempList[1])) + conflictCommands;
//                }
//                }
//                }
//                }
//                return conflictCommands;
