package com.wisesupport.learn.lc.tree;

import org.junit.Test;

import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class SerializeWithIterator extends Base {

    @Test
    public void test() {
        System.out.println(deserialize(serialize(f)));
    }

    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {
        if (root == null) {
            return "[]";
        }
        int depth = new RecursiveTraversal().maxDepth(root);
        String[] array = new String[(int) (Math.pow(2, depth) - 1)];
        LinkedList<Entity> stack = new LinkedList<>();
        boolean isLeft = false;
        int parentIndex = -1;
        do {
            while (root != null) {
                Entity entity;
                if (isLeft) {
                    entity = new Entity(2 * parentIndex + 1, root);
                } else {
                    entity = new Entity(2 * parentIndex + 2, root);
                }
                stack.push(entity);
                parentIndex = entity.position;
                root = root.left;
                isLeft = true;
            }

            if (stack.isEmpty()) {
                break;
            }
            Entity top = stack.pop();
            array[top.position] = String.valueOf(top.node.val);
            if (top.node.right != null) {
                parentIndex = top.position;
                root = top.node.right;
                isLeft = false;
            }
        } while (true);

        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            builder.append(',');
            builder.append(array[i] == null ? "null" : array[i]);
        }
        builder.append(']');
        return builder.toString();
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
        if (data == null) {
            return null;
        }
        TreeMap<Integer, Object> mapping = new TreeMap<>();
        StringTokenizer tokenizer = new StringTokenizer(data, "[],");
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            String value = tokenizer.nextToken();
            if (!"null".equals(value)) {
                mapping.put(index, value);
            }
            index++;
        }

        if (mapping.isEmpty()) {
            return null;
        }
        for (Map.Entry<Integer, Object> entry : mapping.descendingMap().entrySet()) {
            TreeNode node = new TreeNode(
                    ((String) entry.getValue()).charAt(0),
                    (TreeNode) mapping.get(entry.getKey() * 2 + 1),
                    (TreeNode) mapping.get(entry.getKey() * 2 + 2)
            );
            mapping.put(entry.getKey(),node);
        }
        return (TreeNode) mapping.get(0);
    }

    private static class Entity {
        int position;
        TreeNode node;

        Entity(int position, TreeNode node) {
            this.position = position;
            this.node = node;
        }
    }
}
