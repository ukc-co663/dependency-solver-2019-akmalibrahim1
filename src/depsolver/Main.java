package depsolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
    List<String> constraints = JSON.parseObject(readFile(args[2]), strListType);
    Resolver r = new Resolver(repo, constraints);

    List<String> finalState = r.Run();
    List<SatPackage> finalPackages = generateFinalStatePackage(finalState, repo);
//    finalPackages.forEach(f -> System.out.println(f.toString()));
    HashMap<String, SatPackage> hashedRepo = generateHashedRepo(initial, repo);
    Commands commands = new Commands(hashedRepo, finalPackages, constraints);
    List<String> result = commands.BuildCommandsList();
    JSONArray mJSONArray = new JSONArray();
    mJSONArray.addAll(result);
    System.out.println(mJSONArray.toJSONString());
  }

  static String readFile(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename));
    StringBuilder sb = new StringBuilder();
    br.lines().forEach(line -> sb.append(line));
    return sb.toString();
  }

  static List<SatPackage> generateFinalStatePackage(List<String> finalState, List<Package> repo){
    List<SatPackage> result = new ArrayList<>();
    for(String p : finalState){
      String [] packageInfo = p.split(Constants.EQUAL);
      result.add(new SatPackage(packageInfo[0], packageInfo[1]));
    }
    result.forEach(p -> {
      p.setDependenciesAndConflicts(result, repo);
    });
    return result;
  }

  static HashMap<String, SatPackage> generateHashedRepo(List<String> initial, List<Package> repo){
    List<SatPackage> result = new ArrayList<>();
    for(String p : initial){
      String temp = p.replace("+", "");
      String [] packageInfo = temp.split(Constants.EQUAL);
      result.add(new SatPackage(packageInfo[0], packageInfo[1]));
    }

    HashMap<String, SatPackage> hashedRepo = new HashMap<>();

    result.forEach(p -> {
      p.setDependenciesAndConflicts(result, repo);
      hashedRepo.put(p.getPackageName() + "=" + p.getPackageVersion(), p);
    });

    return hashedRepo;
  }

}
