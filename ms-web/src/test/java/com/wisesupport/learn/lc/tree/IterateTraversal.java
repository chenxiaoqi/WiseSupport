package com.wisesupport.learn.lc.tree;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;

public class IterateTraversal extends Base {
    @Test
    public void test() {
        System.out.println("postOrderIterator(f) = " + postOrderIterator(f));

        System.out.println("inOrderIterator(f) = " + inOrderIterator(f));

        System.out.println("levelOrder(f) = " + levelOrder(f));

        Assert.assertThat("has a path sum = F + B + D + E", true, is(hasPathSum(f, 'F' + 'B' + 'D' + 'E')));
    }

    private boolean hasPathSum(TreeNode root, int sum) {
        if (root == null) {
            return false;
        }
        LinkedList<TreeNode> stack = new LinkedList<>();
        while (true) {
            while (root != null) {
                stack.push(root);
                root = root.left;
            }
            if (stack.isEmpty()) {
                break;
            }

            TreeNode top = stack.peek();
            if (top.right != null) {
                root = top.right;
            } else {
                if (top.left == null) {
                    int total = 0;
                    for (TreeNode node : stack) {
                        total += node.val;
                    }
                    if (total == sum) {
                        return true;
                    }
                }
                top = stack.pop();
                while (stack.peek() != null && stack.peek().right != null && stack.peek().right == top) {
                    top = stack.pop();
                }
            }
        }
        return false;
    }

    private List<TreeNode> postOrderIterator(TreeNode node) {
        List<TreeNode> result = new ArrayList<>();
        LinkedList<TreeNode> stack = new LinkedList<>();
        stack.push(node);
        TreeNode prePop = null;
        while (!stack.isEmpty()) {
            TreeNode top = stack.peek();
            if (top.left != null && top.left != prePop) {
                stack.push(top.left);
            } else {
                if (top.right != null) {
                    stack.push(top.right);
                } else {
                    result.add(top);
                    top = stack.pop();
                    while (!stack.isEmpty() && stack.peek().right != null && stack.peek().right == top) {
                        top = stack.pop();
                        result.add(top);
                    }
                    prePop = top;
                }
            }
        }
        return result;
    }



    public List<TreeNode> inOrderIterator(TreeNode node) {
        List<TreeNode> result = new ArrayList<>();
        LinkedList<TreeNode> stack = new LinkedList<>();
        TreeNode root = node;
        do {
            while (root != null) {
                stack.push(root);
                root = root.left;
            }
            if (stack.isEmpty()) {
                break;
            }
            TreeNode top = stack.pop();
            result.add(top);
            root = top.right;
        } while (true);

        return result;
    }

    List<List<Character>> levelOrder(TreeNode root) {
        List<TreeNode> queue = new LinkedList<>();
        List<List<Character>> result = new ArrayList<>();
        queue.add(root);
        do {
            List<Character> level = queue.stream().map(node -> node.val).collect(Collectors.toList());
            result.add(level);
            List<TreeNode> nodes = new ArrayList<>(queue);
            queue.clear();
            for (TreeNode node : nodes) {
                if (node.left != null) {
                    queue.add(node.left);
                }
                if (node.right != null) {
                    queue.add(node.right);
                }
            }
        } while (!queue.isEmpty());
        return result;
    }
}
