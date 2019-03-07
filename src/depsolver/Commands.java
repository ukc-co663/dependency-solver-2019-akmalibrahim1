package depsolver;

import java.util.*;

public class Commands {
    private List<Package> repo;
    private List<String> inital;
    private List<String> finalState;
    private HashSet<Package> waitingList;
    private LinkedHashMap<String, String> finalCommands;
    public Commands (List<Package> repo, List<String> inital, List<String> finalState){
        this.repo = repo;
        this.inital = inital;
        this.finalState = finalState;
        waitingList = new HashSet<>();
        finalCommands = new LinkedHashMap<>();
    }

    //TODO Complete command calls to create a final list of commands
    public void BuildCommandsList() {
        for(String s : finalState){
            String [] nxt = s.split(Constants.EQUAL);
            Package p = Package.getPackage(repo, nxt[0], nxt[1]);
            if(inital.contains(createInstallCommand(p))){

            }
        }
    }

    //TODO Install necessary dependencies
    public boolean isDependenciesInstalled(Package p){
        for(List<String> dep : p.getDepends()){

        }
        return false;
    }

    //TODO Complete uninstallation process
    public String uninstallDependencies(Package p){
        if(p.getConflicts().isEmpty()){
            System.out.println("No Packages uninstall");
        } else {
            String uninstall = "";
        }
        return null;
    }

    static String createInstallCommand(Package p){
        return "+" + p.getName() + "=" + p.getVersion();
    }
}