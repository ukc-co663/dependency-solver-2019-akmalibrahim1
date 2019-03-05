package depsolver;

public class SatPackage {
    private String version;
    private String packageName;

    public SatPackage(String version, String packageName){
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
