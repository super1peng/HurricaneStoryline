package fiu.kdrg.storyline2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import fiu.kdrg.storyline.DoubleMatrix;
import fiu.kdrg.storyline.Edge;
import fiu.kdrg.storyline.SteinerTree;
import fiu.kdrg.storyline.event.Event;

public class SteinerTreeGenerator {

  private List<Event> events;
  private double[][] simGraph = null;
  private List<Integer> doms = null;
  private int k = 12;
  List<Map<Integer, DijkstraShortestPath<Integer, Edge>>> shortestPathsFromAllNodesToDoms;
  SteinerTree steinerTree = null;

  public SteinerTreeGenerator(List<Event> events) {
    // TODO Auto-generated constructor stub
    this.events = events;
  }

  private void genSimGraph() {
    Map<String, Integer> idf = new HashMap<String, Integer>();
    List<Map<String, Double>> X = new ArrayList<Map<String, Double>>();
    simGraph = new double[events.size()][events.size()];
    for (Event event : events) {
      for (String ngram : new HashSet<String>(event.getNGramsOfContent())) {
        Integer df = idf.get(ngram);
        if (df == null)
          df = 0;
        idf.put(ngram, df + 1);// 其实这个是Collection Frequency,不是document frequency
      }
    }

    System.err.println("idf done");

    for (Event event : events) {
      Map<String, Double> x = new HashMap<String, Double>();
      for (String ngram : event.getNGramsOfContent()) {
        Integer ngramidf = idf.get(ngram);
        if (ngramidf != null && ngram.equals(1))
          continue;

        Double tf = x.get(ngram);
        if (tf == null)
          tf = 0.0;
        x.put(ngram, tf + 1);
      }

      double norm = 0;

      for (Entry<String, Double> en : x.entrySet()) {
        String ngram = en.getKey();
        Integer ngramidf = idf.get(ngram);
        Double tf = en.getValue();
        tf *= Math.log((events.size() + 1.0) / ngramidf);
        en.setValue(tf);
        norm += tf * tf;// tf 即是tf-idf
      }
      norm = Math.sqrt(norm);
      for (Entry<String, Double> en : x.entrySet()) {
        en.setValue(en.getValue() / norm);
      }
      X.add(x);
    }

    System.err.println("vectorization done");
    long start = new Date().getTime();

    for (int i = 0; i < events.size(); i++) {
      simGraph[i][i] = 1;
      for (int j = i + 1; j < events.size(); j++) {
        simGraph[i][j] = simGraph[j][i] = innerProduct(X.get(i), X.get(j));
      }
    }
    System.err.println("sim graph done, " + (new Date().getTime() - start)
        / 1000);
  }

  protected void getDomSet(int k) {
    doms = new ArrayList<Integer>();
    Map<Integer, List<Integer>> clusters = new HashMap<Integer, List<Integer>>();

    int n = events.size();
    DoubleMatrix connGraph = DoubleMatrix.zeros(n, n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (simGraph[i][j] > 0.7) {
          connGraph.put(i, j, 1);
          // System.out.println(1);
        }
      }
    }

    DoubleMatrix uncovered = DoubleMatrix.ones(1, n);
    int i = 0;
    while (i < k) {
      DoubleMatrix covering = DoubleMatrix.ones(1, n);
      covering.copy(uncovered);
      covering = covering.mmul(connGraph);
      int sel = covering.argmax();
      double maxdeg = covering.get(sel);

      if (maxdeg < 0.5)
        break;

      doms.add(sel);

      DoubleMatrix ind = connGraph.getRow(sel).ge(0.5);
      List<Integer> members = new ArrayList<Integer>();
      for (int rowN = 0; rowN < ind.rows; rowN++)
        for (int colN = 0; colN < ind.columns; colN++) {
          if (ind.get(rowN, colN) != 0)
            members.add(colN);
        }

      clusters.put(sel, members);

      int[] neighbors = ind.findIndices();
      uncovered = uncovered.put(0, ind, 0);
      i++;
    }
  }

  private DirectedGraph<Integer, Edge> genConnGraph() {
    DirectedGraph<Integer, Edge> connGraph = new DefaultDirectedGraph<Integer, Edge>(
        Edge.class);
    for (int i = 0; i < simGraph.length; i++)
      connGraph.addVertex(i);

    for (int i = 0; i < simGraph.length; i++) {
      long ti = events.get(i).getEventDate();
      for (int j = 0; j < simGraph.length; j++) {
        if (i == j)
          continue;
        if (simGraph[i][j] <= 0.1) {
          continue;
        }

        // 相差50个小时，相似度大于0.1
        long tj = events.get(j).getEventDate();
        if (ti <= tj && ti > tj - 1000 * 3600 * 50) {
          connGraph.addEdge(i, j);
        }
      }
    }
    return connGraph;
  }

  protected void getSteinerTree(int approx) {
    Collections.sort(doms, new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return (int) (events.get(o1).getEventDate() - events.get(o2)
            .getEventDate());
      }
    });

    genConnGraph();
    shortestPathsFromAllNodesToDoms = new ArrayList<Map<Integer, DijkstraShortestPath<Integer, Edge>>>();
    for (int i = 0; i < simGraph.length; i++) {
      Map<Integer, DijkstraShortestPath<Integer, Edge>> paths = new HashMap<Integer, DijkstraShortestPath<Integer, Edge>>();
      for (int j = 0; j < doms.size(); j++) {
        DijkstraShortestPath<Integer, Edge> path = new DijkstraShortestPath<Integer, Edge>(
            connGraph, i, doms.get(j));
        path.getPath();
        paths.put(doms.get(j), path);
      }
      shortestPathsFromAllNodesToDoms.add(paths);
    }

    int kk = doms.size();
    // doms.get(0),最早的dom元素做为root
    // 为了使图形的SteinerTree信息保存下来，在SteinerTree类里面加入图信息（树？）
    SteinerTree tree = getSteinerTree(doms, doms.get(0), kk--, approx);
    while (tree == null) {
      tree = getSteinerTree(doms, doms.get(0), kk--, approx);
    }
    steinerTree = tree;
  }

  
  
  protected SteinerTree getSteinerTree(List<Integer> doms, int root, int k,
      int si) {
    if (si == 1)
      return getBaseSteinerTree(doms, root, k);
    int n = connGraph.vertexSet().size();
    SteinerTree tree = new SteinerTree(doms);
    while (k > 0) {
      SteinerTree besttree = new SteinerTree(doms);
      besttree.add(root);
      for (Edge e : connGraph.outgoingEdgesOf(root)) {
        int v = connGraph.getEdgeTarget(e);
        for (int kp = 1; kp <= k; kp++) {
          SteinerTree ctree = getSteinerTree(doms, v, kp, si - 1);
          if (ctree == null)
            continue;
          ctree.add(root);// 这个地方应该加条root到v的边
          ctree.graphSteinerTreeInfo.addEdge(root, v);
          if (ctree.cost() < besttree.cost())// cost有问题
            besttree = ctree;
        }
      }
      if (besttree.cover() == 0)
        return null;
      k -= besttree.cover();
      doms = new ArrayList<Integer>(doms);
      doms.removeAll(besttree.getDoms());

      tree.add(besttree);
    }
    return tree;
  }
  

  protected SteinerTree getBaseSteinerTree(List<Integer> doms, final int root,
      int k) {
    final Map<Integer, DijkstraShortestPath<Integer, Edge>> paths = this.shortestPathsFromAllNodesToDoms
        .get(root);

    SteinerTree tree = new SteinerTree(doms);
    tree.add(root);
    List<Integer> targets = new ArrayList<Integer>(doms);
    targets.remove(new Integer(root));
    Collections.sort(targets, new Comparator<Integer>() {

      @Override
      // 从root到dom的最短路径排序。
      public int compare(Integer o1, Integer o2) {
        int l1 = 100000;
        int l2 = 100000;
        List<Edge> path = paths.get(o1).getPathEdgeList(); // shortestPath.getPathEdgeList(o1);
        if (path != null)
          l1 = path.size();
        path = paths.get(o2).getPathEdgeList();
        if (path != null)
          l2 = path.size();

        return l1 - l2;
      }
    });

    // 这不一定是最优的最短路径集合。在这个地方可构造出对应的steinerTree（详细的图，而不是只有点和cover的点信息）。
    for (int i = 0; i < targets.size(); i++) {
      if (tree.cover() == k)
        break;
      int nodei = targets.get(i);
      if (paths.get(nodei).getPathEdgeList() == null)
        break;
      for (Edge e : paths.get(nodei).getPathEdgeList()) {// 最短路径生成的不一定是颗树？？？
        Integer target = connGraph.getEdgeTarget(e);
        Integer source = connGraph.getEdgeSource(e);
        tree.add(target);
        tree.getGraphSteinerTreeInfo().addVertex(source);
        tree.getGraphSteinerTreeInfo().addVertex(target);
        tree.getGraphSteinerTreeInfo().addEdge(source, target);

      }
    }

    if (tree.cover() != k)
      return null;
    else
      return tree;
  }

  private void setEventWeights() {

    int n = simGraph.length;
    DoubleMatrix simGraphMatrix = new DoubleMatrix(simGraph);
    DoubleMatrix addMatrix = DoubleMatrix.ones(1, n);
    DoubleMatrix weights = DoubleMatrix.zeros(1, n);
    weights = addMatrix.mmul(simGraphMatrix);

    for (int i = 0; i < n; i++) {
      events.get(i).setWeight(weights.get(i));
    }
  }

  static public double innerProduct(Map<String, Double> a, Map<String, Double> b) {
    double inner = 0;
    if (a.size() > b.size()) {
      Map<String, Double> c = a;
      a = b;
      b = c;
    }
    for (Entry<String, Double> en : a.entrySet()) {
      Double v = b.get(en.getKey());
      if (v != null)
        inner += en.getValue() * v;
    }
    return inner;
  }

}
