package depsolver;

import com.microsoft.z3.*;

import java.util.*;

public class Resolver {
    private List<Package> repo;
    private Solver solver;
    private Context ctx;
    private List<String> con;
    private List<BoolExpr> finalExpression;
    private HashSet<Package> usedRepo;

    public Resolver(List<Package> repo, List<String> constraints){
        finalExpression = new ArrayList<>();
        usedRepo = new HashSet<>();
        ctx = new Context();
        solver = ctx.mkSolver();
        con = constraints;
        this.repo = repo;

    }

    public void Run(){
        for(String constr : con){
            if(constr.contains(Constants.INCLUDE)){
                String temp = constr.replace(Constants.INCLUDE, "");
                List<Package> current  = getPackageVersions(temp);
                List<BoolExpr> include = new ArrayList<>();
                List<BoolExpr> implicationList = new ArrayList<>();
                for(Package p : current) {
                    usedRepo.add(p);
                    BoolExpr currentPackage = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
                    include.add(currentPackage);
                    List<BoolExpr> implication = getAllDependencyConstraints(p, p.getDepends());
                    implication.add(buildConflicts(currentPackage, p.getConflicts()));
                    implicationList.add(ctx.mkImplies(currentPackage, ctx.mkAnd(implication.toArray(new BoolExpr [implication.size()]))));
                }
                finalExpression.add(ctx.mkOr(include.toArray(new BoolExpr[include.size()])));
                finalExpression.add(ctx.mkOr(implicationList.toArray(new BoolExpr[implicationList.size()])));
            } else {
                String temp = constr.replace(Constants.EXCLUDE, "");
                List<Package> current  = getPackageVersions(temp);
                List<BoolExpr> exclude = new ArrayList<>();
                for(Package p : current){
                    usedRepo.add(p);
                    BoolExpr c = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
                    exclude.add(ctx.mkNot(c));
                }
            }
        }

        for(Package p : repo){
            if(!usedRepo.contains(p)){
                usedRepo.add(p);
                BoolExpr currentPackage = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
                List<BoolExpr> implication = getAllDependencyConstraints(p, p.getDepends());
                implication.add(buildConflicts(currentPackage, p.getConflicts()));
                finalExpression.add(ctx.mkImplies(currentPackage, ctx.mkAnd(implication.toArray(new BoolExpr [implication.size()]))));
            }
        }
        BoolExpr result = ctx.mkAnd(finalExpression.toArray(new BoolExpr[finalExpression.size()]));
        solver.add(result);
        if(solver.check() == Status.SATISFIABLE){
            System.out.println("Result Available");
            Model m = solver.getModel();
            List<FuncDecl> dec = new ArrayList<>(Arrays.asList(m.getDecls()));
            dec.forEach(d -> System.out.println(d.getName().toString()));
            System.out.println(m.toString());
        } else {
            System.out.println("Result Unavailable");
        }
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

        List<Package> packages = new ArrayList<>();

        if(comparator.isEmpty()){
            for(Package sp : repo){
                if(p.equals(sp.getName())){
                    packages.add(sp);
                }
            }
        } else {
            String[] versionList = p.split(comparator);
            for(Package sp: repo){
                if (versionList[0].equals(sp.getName()) && Package.checkVersion(versionList[1], sp.getVersion(), comparator)) {
                    packages.add(sp);
                }
            }
        }


        return packages;
    }

    public List<BoolExpr> getAllDependencyConstraints(Package current, List<List<String>> packages){
        List<BoolExpr> constraint = new ArrayList<>();

        for(List<String> pak : packages){

                List<BoolExpr> option = new ArrayList<>();
                pak.forEach(p -> option.add(buildOrs(getPackageVersions(p))));
                constraint.add(ctx.mkOr(option.toArray(new BoolExpr[option.size()])));
        }
        return constraint;
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
        for (Package c : conflictList) {
            BoolExpr d = ctx.mkBoolConst(c.getName() + "=" + c.getVersion());
            result.add(ctx.mkNot(d));
        }

        return ctx.mkAnd(result.toArray(new BoolExpr[result.size()]));
    }


}
