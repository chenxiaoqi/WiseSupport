package com.wisesupport.learn.lc.tree;

import org.junit.Test;

import java.util.List;

public class Sort extends Base {

    @Test
    public void test() {
        System.out.println("isValidBST(f) = " + isValidBST(f));
    }

    public boolean isValidBST(TreeNode root) {

        List<TreeNode> list = new IterateTraversal().inOrderIterator(root);
        TreeNode pre = null;
        for (TreeNode node : list) {
            if (pre != null) {
                if (node.val < pre.val) {
                    return false;
                }
            }
            pre = node;
        }
        return true;
    }
}
