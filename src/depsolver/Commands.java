package depsolver;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.util.*;

public class Commands {
    private HashMap<String, SatPackage> initial;
//    private List<String> inital;
    private List<SatPackage> finalState;
    private Stack<SatPackage> waitingList;
    private LinkedList<String> finalCommands;
    public Commands (HashMap<String, SatPackage> repo, List<SatPackage> finalState){
        this.initial = repo;
        this.finalState = finalState;
        waitingList = new Stack<>();
        finalCommands = new LinkedList<>();
    }

    //TODO Complete command calls to create a final list of commands
    public void BuildCommandsList() {
        for(SatPackage s : finalState){
            if(initial.containsKey(createInstallCommand(s))){
                checkDependenciesInstalled(s);
            }
        }
    }

    //TODO Install necessary dependencies
    public boolean checkDependenciesInstalled(SatPackage p){
        for(HashSet<SatPackage> s : p.getDependencies()){
            for(SatPackage sat: s) {
                if (!initial.containsKey(createPackageVersionKey(sat))) {
                    //Uninstall
                }
            }
        }
        return false;
    }

    public void uninstallConflicts(SatPackage p){
    }

    public void uninstallDependencies(SatPackage p){
        if(p.getDependents().size() < 0){
            finalCommands.add(createUninstallCommand(p));
            initial.remove(createPackageVersionKey(p));
        } else {
           Iterator<SatPackage> ite = p.getDependents().iterator();
           while(ite.hasNext()){
               SatPackage s = ite.next();
               if(checkForOnlyDependency(s, p)){
                   uninstallDependencies(s);
               }
               initial.remove(createPackageVersionKey(s));
               finalCommands.add(createUninstallCommand(s));
           }
        }
    }

    /**
     * Checks if this is this package only relies on one dependent
     * @param p Current package
     * @param dep The package dependency
     * @return true if it is the only dependency false if not
     */
    public boolean checkForOnlyDependency(SatPackage p, SatPackage dep){
        for(HashSet<SatPackage> sat : p.getDependencies()) {
            if(sat.contains(dep) && sat.size() > 1){
                return true;
            }
        }
        return false;
    }

    static String createPackageVersionKey(SatPackage p){
        return p.getPackageName() + "=" + p.getPackageVersion();
    }

    static String createInstallCommand(SatPackage p){
        return "+" + createPackageVersionKey(p);
    }

    static String createUninstallCommand(SatPackage p){
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
