package depsolver;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Probe;
import com.microsoft.z3.Solver;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.tools.internal.jxc.ap.Const;

import java.util.*;

public class Resolver {
    private List<Package> repo;
    private Solver solver;
    private Context ctx;
    private List<String> con;
    private List<BoolExpr> finalExpression;
    private HashSet<Package> usedRepo;

    public Resolver(List<Package> repo, List<String> constraints){
        this.repo = repo;
        ctx = new Context();
        solver = ctx.mkSolver();
        con = constraints;
        finalExpression = new ArrayList<>();
        usedRepo = new HashSet<>();
    }

    public void Run(){
        for(String constr : con){
            if(constr.contains(Constants.INCLUDE)){
                String temp = constr.replace(Constants.INCLUDE, " ");
                List<Package> current  = getPackageVersions(temp);
                List<BoolExpr> include = new ArrayList<>();
                List<BoolExpr> implicationList = new ArrayList<>();
                for(Package p : current) {
                    usedRepo.add(p);
                    BoolExpr currentPackage = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
                    include.add(currentPackage);
                    BoolExpr implication = getAllDependencyConstraints(p, p.getDepends());
                    BoolExpr conflicts = buildConflicts(currentPackage, p.getConflicts());
                    implicationList.add(ctx.mkAnd(implication, currentPackage, conflicts));
                }
                finalExpression.add(ctx.mkOr(include.toArray(new BoolExpr[include.size()])));
                finalExpression.add(ctx.mkOr(implicationList.toArray(new BoolExpr[implicationList.size()])));
            } else {
                String temp = constr.replace(Constants.EXCLUDE, " ");
                List<Package> current  = getPackageVersions(temp);
                List<BoolExpr> exclude = new ArrayList<>();
                for(Package p : current){
                    usedRepo.add(p);
                    BoolExpr c = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
                    exclude.add(ctx.mkNot(c));
                }
                finalExpression.addAll(exclude);
            }
        }

        solver.add(ctx.mkAnd(finalExpression.toArray(new BoolExpr[finalExpression.size()])));
    }

    public void findDependencies(Package p){
        List<Package> packages = new ArrayList<>();
        for(Package r : repo){
            if(r.getName().equals(p.getName())){
                packages.add(r);
            }

        }
    }

    public List<Package> getPackageVersions(String p){
        String comparator = "";

        if(p.contains(Constants.EQUAL)){
            comparator = Constants.EQUAL;
        } else if(p.contains(Constants.GREATER_OR_EQUAL)){
            comparator = Constants.GREATER_OR_EQUAL;
        } else if(p.contains(Constants.LESS_OR_EQUAL)){
            comparator = Constants.LESS_OR_EQUAL;
        } else if(p.contains(Constants.GREATER_THAN)) {
            comparator = Constants.GREATER_THAN;
        } else if(p.contains(Constants.LESS_THAN)){
            comparator = Constants.LESS_THAN;
        }

        String[] versionList = p.split(comparator);

        List<Package> packages = new ArrayList<>();

        for(Package sp: repo){
            if(versionList.length == 1 && versionList[0] == sp.getName()){
                packages.add(sp);
            } else if (sp.getName() == versionList[0] && Package.checkVersion(versionList[1], sp.getVersion(), comparator)) {
                packages.add(sp);
            }
        }

        return packages;
    }

    public BoolExpr getAllDependencyConstraints(Package current, List<List<String>> packages){
        BoolExpr [] constraint = new BoolExpr[packages.size()];
        for(int i = 0; i < packages.size(); i++){

            if(packages.get(i).size() == 1){
                constraint[i] = buildOrs(getPackageVersions(packages.get(i).get(0)));
            } else{
                List<BoolExpr> option = new ArrayList<>();
                packages.get(i).forEach(p -> option.add(buildOrs(getPackageVersions(p))));
                constraint[i] = ctx.mkOr(option.toArray(new BoolExpr[option.size()]));
            }
        }

        BoolExpr ands = ctx.mkAnd(constraint);
        BoolExpr currentBool = ctx.mkBoolConst(current.getName() + "=" + current.getVersion());

        return ctx.mkAnd(currentBool, ands);
    }

    /**
     * Create expression which implies the targeted package requires one of the following packages
     * @param packages A list of packages of the same name but different versions
     * @return
     */
    private BoolExpr buildOrs(List<Package> packages){
        BoolExpr [] dependencies = new BoolExpr[packages.size()];
        for(int i = 0; i < packages.size(); i++){
            Package p = packages.get(i);
            dependencies[i] = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
        }

        return ctx.mkOr(dependencies);
    }

    private BoolExpr buildConflicts(BoolExpr current, List<String> conflicts){
        List<BoolExpr> result = new ArrayList<>();
        List<Package> conflictList = new ArrayList<>();
        for(String c : conflicts){
            conflictList.addAll(getPackageVersions(c));
        }
        result.add(current);
        for (Package c : conflictList) {
            BoolExpr d = ctx.mkBoolConst(c.getName() + "=" + c.getVersion());
            result.add(ctx.mkNot(d));
        }

        return ctx.mkAnd(result.toArray(new BoolExpr[result.size()]));
    }


}
