package com.wisesupport.learn.algorithm;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class Graphic {

    @Test
    public void test() {
        Assert.assertThat(byPiano(), Matchers.is(35));
    }

    private int byPiano() {
        Map<String, CostEntry> costs = new HashMap<>();
        Map<String, List<Edge>> g = create();
        Queue<Edge> queue = new LinkedList<>(g.get("MusicScore"));
        while (!queue.isEmpty()) {
            Edge edge = queue.poll();
            CostEntry parent = costs.get(edge.getStart());
            int parentCost = 0;
            if (parent != null) {
                parentCost = parent.getCost();
            }
            int cost = parentCost + edge.getCost();
            CostEntry cur = costs.get(edge.getEnd());
            if (cur == null) {
                costs.put(edge.getEnd(), new CostEntry(cost, edge));
            }else{
                if (cost < cur.getCost()) {
                    costs.put(edge.getEnd(), new CostEntry(cost, edge));
                }
            }

            List<Edge> edges = g.get(edge.getEnd());
            if (edges != null) {
                queue.addAll(edges);
            }
        }
        LinkedList<String> paths = new LinkedList<>();
        String start = "Piano";
        paths.addFirst(start);
        while (start != null) {
            CostEntry entry = costs.get(start);
            if (entry != null) {
                paths.addFirst(entry.getEdge().getStart());
                start = entry.getEdge().getStart();
            }else{
                start = null;
            }
        }
        System.out.println(paths);
        return costs.get("Piano").getCost();
    }

    private Map<String, List<Edge>> create() {
        Map<String, List<Edge>> g = new HashMap<>();
        g.put("MusicScore", Arrays.asList(
                new Edge("MusicScore", "Record", 5),
                new Edge("MusicScore", "Poster", 0)
        ));
        g.put("Poster", Arrays.asList(
                new Edge("Poster", "Guitar", 30),
                new Edge("Poster", "Drum", 35)
        ));
        g.put("Record", Arrays.asList(
                new Edge("Record", "Guitar", 15),
                new Edge("Record", "Drum", 20)
        ));
        g.put("Guitar", Collections.singletonList(
                new Edge("Guitar", "Piano", 20)
        ));
        g.put("Drum", Collections.singletonList(
                new Edge("Drum", "Piano", 10)
        ));
        return g;
    }

    private static class CostEntry{
        private int cost;
        private Edge edge;

        public CostEntry(int cost, Edge edge) {
            this.cost = cost;
            this.edge = edge;
        }

        public int getCost() {
            return cost;
        }

        public Edge getEdge() {
            return edge;
        }
    }

    private static class Edge {
        private String start;
        private String end;
        private int cost;

        public Edge(String start, String end, int cost) {
            this.start = start;
            this.end = end;
            this.cost = cost;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }

        public int getCost() {
            return cost;
        }
    }

}
