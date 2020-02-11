package com.wisesupport.learn.lc.tree;

class TreeNode {
    char val;
    TreeNode left;
    TreeNode right;

    TreeNode(char val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode node = (TreeNode) o;
        return val == node.val;
    }

    @Override
    public String toString() {
        return String.valueOf(val);
    }
}
