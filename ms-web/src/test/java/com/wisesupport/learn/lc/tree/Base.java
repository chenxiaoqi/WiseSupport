package com.wisesupport.learn.lc.tree;

public class Base {
    protected TreeNode f;
    
    public Base() {

        //         F
        //       /  \
        //     B      G
        //   /  \       \
        //  A    D        I
        //     /   \     /
        //    C     E   H
        TreeNode c = new TreeNode('C', null, null);
        TreeNode e = new TreeNode('E', null, null);
        TreeNode d = new TreeNode('D', c, e);
        TreeNode a = new TreeNode('A', null, null);
        TreeNode b = new TreeNode('B', a, d);


        TreeNode h = new TreeNode('H', null, null);
        TreeNode i = new TreeNode('I', h, null);
        TreeNode g = new TreeNode('G', null, i);
        f = new TreeNode('F', b, g);
    }
}
