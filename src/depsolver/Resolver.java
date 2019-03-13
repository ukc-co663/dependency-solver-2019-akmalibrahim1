package depsolver;

import com.microsoft.z3.*;
import com.microsoft.z3.enumerations.Z3_lbool;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Resolver {
    private List<Package> repo;
    private Solver solver;
    private Context ctx;
    private List<String> con;
    private List<BoolExpr> expressionsList;
    private HashSet<Package> usedRepo;
    public Queue<Package> dependencies;

    public Resolver(List<Package> repo, List<String> constraints) {
        expressionsList = new ArrayList<>();
        usedRepo = new HashSet<>();
        ctx = new Context();
        solver = ctx.mkSolver();
        con = constraints;
        this.repo = repo;
        dependencies = new ConcurrentLinkedQueue<>();
    }

    //TODO Fix concurrent error possibly split adding and removing to 2 separate branches
    /**
     * Finds the final state
     *
     * @return A final state in a string list or null if none was found
     */
    public List<String> Run() {
        for (String constr : con) {
            if (constr.contains(Constants.INCLUDE)) {
                String temp = constr.replace(Constants.INCLUDE, "");
                List<Package> current = getPackageVersions(temp);
                List<BoolExpr> include = new ArrayList<>();
                List<BoolExpr> implicationList = new ArrayList<>();
                for (Package p : current) {
                    usedRepo.add(p);
                    BoolExpr currentPackage = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
                    include.add(currentPackage);
                    List<BoolExpr> implication = getAllDependencyConstraints(p, p.getDepends());
                    implication.add(buildConflicts(currentPackage, p.getConflicts()));
                    implicationList.add(ctx.mkImplies(currentPackage, ctx.mkAnd(implication.toArray(new BoolExpr[implication.size()]))));
                }
                expressionsList.add(ctx.mkOr(include.toArray(new BoolExpr[include.size()])));
                expressionsList.add(ctx.mkAnd(implicationList.toArray(new BoolExpr[implicationList.size()])));
            } else {
                String temp = constr.replace(Constants.EXCLUDE, "");
                List<Package> current = getPackageVersions(temp);
                List<BoolExpr> exclude = new ArrayList<>();
                for (Package p : current) {
                    usedRepo.add(p);
                    BoolExpr c = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
                    exclude.add(ctx.mkNot(c));
                }
            }
        }


        int size = dependencies.size();
        while (!dependencies.isEmpty()) {
            Package p = dependencies.remove();
            if (!usedRepo.contains(p)) {
                usedRepo.add(p);
                BoolExpr currentPackage = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
                List<BoolExpr> implication = getAllDependencyConstraints(p, p.getDepends());
                implication.add(buildConflicts(currentPackage, p.getConflicts()));
                expressionsList.add(ctx.mkImplies(currentPackage, ctx.mkAnd(implication.toArray(new BoolExpr[implication.size()]))));
            }
        }

        BoolExpr finalExpression = ctx.mkAnd(expressionsList.toArray(new BoolExpr[expressionsList.size()]));
        solver.add(finalExpression);
        if (solver.check() == Status.SATISFIABLE) {
            Model m = solver.getModel();
            List<FuncDecl> dec = new ArrayList<>(Arrays.asList(m.getConstDecls()));
            List<String> result = new ArrayList<>();
            dec.forEach(d -> {
                if (m.getConstInterp(d).getBoolValue() == Z3_lbool.Z3_L_TRUE)
                    result.add(d.getName().toString());
            });
            return result;
        } else {
            return null;
        }
    }


    private List<Package> getPackageVersions(String p) {
        String comparator = Package.getComparator(p);

        List<Package> packages = new ArrayList<>();

        if (comparator.isEmpty()) {
            for (Package sp : repo) {
                if (p.equals(sp.getName())) {
                    packages.add(sp);
                }
            }
        } else {
            String[] versionList = p.split(comparator);
            for (Package sp : repo) {
                if (versionList[0].equals(sp.getName()) && Package.checkVersion(versionList[1], sp.getVersion(), comparator)) {
                    packages.add(sp);
                }
            }
        }
        return packages;
    }

    private List<BoolExpr> getAllDependencyConstraints(Package current, List<List<String>> packages) {
        List<BoolExpr> constraint = new ArrayList<>();

        for (List<String> pak : packages) {
            List<BoolExpr> option = new ArrayList<>();
            pak.forEach(p -> {
                List<Package> deps = getPackageVersions(p);
                dependencies.addAll(deps);
                option.add(buildOrs(deps));
            });
            constraint.add(ctx.mkOr(option.toArray(new BoolExpr[option.size()])));
        }
        return constraint;
    }

    /**
     * Create expression which implies the targeted package requires one of the following packages
     *
     * @param packages A list of packages of the same name but different versions
     * @return
     */
    private BoolExpr buildOrs(List<Package> packages) {
        BoolExpr[] dependencies = new BoolExpr[packages.size()];
        for (int i = 0; i < packages.size(); i++) {
            Package p = packages.get(i);
            dependencies[i] = ctx.mkBoolConst(p.getName() + "=" + p.getVersion());
        }

        return ctx.mkOr(dependencies);
    }

    private BoolExpr buildConflicts(BoolExpr current, List<String> conflicts) {
        List<BoolExpr> result = new ArrayList<>();
        List<Package> conflictList = new ArrayList<>();
        for (String c : conflicts) {
            conflictList.addAll(getPackageVersions(c));
        }
        for (Package c : conflictList) {
            BoolExpr d = ctx.mkBoolConst(c.getName() + "=" + c.getVersion());
            result.add(ctx.mkNot(d));
        }

        return ctx.mkAnd(result.toArray(new BoolExpr[result.size()]));
    }
}
