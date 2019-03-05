package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
  public static void main(String[] args) throws IOException {
    TypeReference<List<Package>> repoType = new TypeReference<List<Package>>() {
    };
    List<Package> repo = JSON.parseObject(readFile(args[0]), repoType);
    TypeReference<List<String>> strListType = new TypeReference<List<String>>() {
    };
    List<String> initial = JSON.parseObject(readFile(args[1]), strListType);
    Resolver r = new Resolver(repo, JSON.parseObject(readFile(args[2]), strListType));
    List<String> con = JSON.parseObject(readFile(args[2]), strListType);
    HashMap<SatPackage, Boolean> constraints = getConstraints(con);
    constraints.forEach((e, f) -> System.out.println(e.toString() + " State = " + f));
  }

  static String readFile(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    StringBuilder sb = new StringBuilder();
    br.lines().forEach(line -> sb.append(line));
    return sb.toString();
  }

  static HashMap<SatPackage, Boolean> getConstraints(List<String> constraints){
      HashMap<SatPackage, Boolean> result = new HashMap<>();
      for(String c : constraints){
        String version = null;
        String cTemp = c;
        String compare = null;
        if(cTemp.contains("=")){
            String [] split = cTemp.split("=");
            cTemp = split[0];
            version = split[1];
            compare = Constants.EQUAL;
        }
            //flag that states whether the package should be installed or removed
            boolean add = false;
            if (cTemp.charAt(0) == '+') {
                add = true;
            }
            result.put(new SatPackage(version, cTemp.substring(1)), add);
      }
      return result;
  }
}


// CHANGE CODE BELOW:
// using repo, initial and constraints, compute a solution and print the answer
//    for (Package p : repo) {
//            System.out.printf("package %s version %s\n", p.getName(), p.getVersion());
//            for (List<String> clause : p.getDepends()) {
//        System.out.printf("  dep:");
//        for (String q : clause) {
//        System.out.printf(" %s", q);
//        }
//        System.out.printf("\n");
//        }
//        }
