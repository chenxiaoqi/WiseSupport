package com.wisesupport.learn.lc.tree;

import org.junit.Test;

public class Serialization extends Base {

    @Test
    public void test() {
        System.out.println(serialize(f));
        TreeNode root = deserialize(serialize(f));
        System.out.println(new IterateTraversal().levelOrder(root));
    }

    public String serialize(TreeNode root) {
        if (root == null) {
            return null;
        }
        int depth = new RecursiveTraversal().maxDepth(root);
        String[] array = new String[(int) (Math.pow(2, depth) - 1)];
        serialize(root, 0, array);
        StringBuilder builder = new StringBuilder();
        builder.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            builder.append(',');
            builder.append(array[i] == null ? "null" : array[i]);
        }
        return builder.toString();
    }

    public TreeNode deserialize(String data) {
        String[] array = data.split(",");
        return deserialize(array, 0);
    }

    private TreeNode deserialize(String[] array, int index) {
        if (index >= array.length) {
            return null;
        }
        String root = array[index];
        if ("null".equals(root)) {
            return null;
        }
        return new TreeNode(root.charAt(0), deserialize(array, 2 * index + 1), deserialize(array, 2 * index + 2));
    }

    private void serialize(TreeNode root, int index, String[] array) {
        array[index] = String.valueOf(root.val);
        if (root.left != null) {
            array[2 * index + 1] = String.valueOf(root.left);
            serialize(root.left, 2 * index + 1, array);
        }
        if (root.right != null) {
            array[2 * index + 2] = String.valueOf(root.right);
            serialize(root.right, 2 * index + 2, array);
        }
    }

}
