package com.wisesupport.learn.lc.tree;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RecursiveTraversal extends Base {

    @Test
    public void test() {
        System.out.println(preOrder(f));
        System.out.println(inOrder(f));
        System.out.println(postOrder(f));
        //是否是镜像树
        System.out.println(isSymmetric(f));

        //是否包含路径和是29
        System.out.println(hasPathSum(f, 29));

        System.out.println(maxDepth(f));
    }

    protected int maxDepth(TreeNode node) {
        if (node.left == null && node.right == null) {
            return 1;
        }
        int leftDepth = 0, rightDepth = 0;
        if (node.left != null) {
            leftDepth = maxDepth(node.left);
        }
        if (node.right != null) {
            rightDepth = maxDepth(node.right);
        }
        return Math.max(leftDepth, rightDepth) + 1;
    }

    public boolean hasPathSum(TreeNode root, int sum) {
        if (root == null) {
            return false;
        }
        sum = sum - root.val;
        if (root.left == null && root.right == null) {
            return sum == 0;
        }
        return hasPathSum(root.left, sum) || hasPathSum(root.right, sum);
    }

    private boolean isSymmetric(TreeNode root) {
        if (root == null) {
            return true;
        }
        return isSymmetric(root.left, root.right);
    }

    private boolean isSymmetric(TreeNode left, TreeNode right) {
        if (left == null && right == null) {
            return true;
        }

        if (left != null && right != null) {
            if (left.val == right.val) {
                return isSymmetric(left.left, right.right) && isSymmetric(left.right, right.left);
            }
        }
        return false;
    }

    private List<TreeNode> preOrder(TreeNode node) {
        List<TreeNode> nodes = new ArrayList<>();
        nodes.add(node);
        if (node.left != null) {
            nodes.addAll(preOrder(node.left));
        }
        if (node.right != null) {
            nodes.addAll(preOrder(node.right));
        }
        return nodes;
    }

    private List<TreeNode> inOrder(TreeNode node) {
        List<TreeNode> nodes = new ArrayList<>();
        if (node.left != null) {
            nodes.addAll(inOrder(node.left));
        }
        nodes.add(node);
        if (node.right != null) {
            nodes.addAll(inOrder(node.right));
        }
        return nodes;
    }

    private List<TreeNode> postOrder(TreeNode node) {
        List<TreeNode> nodes = new ArrayList<>();
        if (node.left != null) {
            nodes.addAll(inOrder(node.left));
        }
        if (node.right != null) {
            nodes.addAll(inOrder(node.right));
        }
        nodes.add(node);
        return nodes;
    }
}
