package depsolver;

public class Constraints {
    private String version;
    private String packageName;

    public Constraints(String version, String packageName){
        this.version = version;
        this.packageName = packageName;
    }

    public String getVersion(){
        return version;
    }

    public String getPackageName(){
        return packageName;
    }

    @Override
    public String toString (){
        return "Package = " + packageName + " Version = " + version;
    }
}
