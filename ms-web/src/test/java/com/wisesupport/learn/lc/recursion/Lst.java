package com.wisesupport.learn.lc.recursion;

import com.sun.org.apache.bcel.internal.generic.RET;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class Lst {

    public Lst() {
    }

    @Test
    public void test() {
        System.out.println(reverseList(create()));
        System.out.println(reverse(create()));

        ListNode n1 = new ListNode(6);
        n1.next = new ListNode(7);
        n1.next.next = new ListNode(8);

        ListNode n2 = new ListNode(1);
        n2.next = new ListNode(3);
        n2.next.next = new ListNode(4);
        System.out.println(mergeTwoLists(n1, n2));

        System.out.println(hasCycle(new ListNode(1)));
    }

    public boolean hasCycle(ListNode head) {
        Set<ListNode> set = new HashSet<>();
        while (head != null) {
            if (!set.add(head)) {
                return true;
            }
            head = head.next;
        }
        return false;
    }
    public ListNode rotateRight(ListNode head, int k) {
        ListNode h = head;
        ListNode t = null;
        int size = 0;
        while (head != null) {
            size++;
            if (head.next == null) {
                t = head;
            }
            head = head.next;
        }
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return h;
        }
        k = k % size;
        k = size - k;

        ListNode nt = h;
        for (int i = 1; i < k; i++) {
            nt = nt.next;
        }

        ListNode nh = nt.next;
        nt.next = null;
        t.next = h;
        return nh;
    }

    public ListNode mergeKLists(ListNode[] lists) {
        if (lists == null || lists.length == 0) {
            return null;
        }
        if (lists.length == 1) {
            return lists[0];
        }
        ListNode node = lists[0];
        for (int i = 1; i < lists.length; i++) {
            ListNode list = lists[i];
            node = mergeTwoLists(node, list);
        }
        return node;
    }

    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if (l1 == null) {
            return l2;
        }
        if (l2 == null) {
            return l1;
        }

        ListNode head = null;
        while (l2 != null) {
            if (l2.val < l1.val) {
                if (head == null) {
                    head = l2;
                }
                ListNode l2n = l2.next;
                l2.next = l1;
                l1 = l2;
                l2 = l2n;

            } else {
                if (head == null) {
                    head = l1;
                }
                if (l1.next == null) {
                    l1.next = l2;
                    break;
                } else {
                    if (l2.val <= l1.next.val || l2.val == l1.val) {
                        ListNode l1n = l1.next;
                        ListNode l2n = l2.next;
                        l1.next = l2;
                        l2.next = l1n;
                        l1 = l2;
                        l2 = l2n;
                    } else {
                        l1 = l1.next;
                    }
                }
            }
        }
        return head;
    }

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        int carry = 0;
        ListNode result = null;
        ListNode cur = null;
        do {
            int left = 0;
            int right = 0;
            if (l1 != null) {
                left = l1.val;
                l1 = l1.next;
            }
            if (l2 != null) {
                right = l2.val;
                l2 = l2.next;
            }
            int sum = left + right + carry;
            carry = sum / 10;
            if (result == null) {
                result = new ListNode(sum % 10);
                cur = result;
            } else {
                ListNode node = new ListNode(sum % 10);
                cur.next = node;
                cur = node;
            }
        } while (l1 != null || l2 != null);
        if (carry == 1) {
            cur.next = new ListNode(1);
        }
        return result;
    }


    public ListNode reverseList(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        } else {
            ListNode list = reverseList(head.next);
            head.next.next = head;
            head.next = null;
            return list;
        }
    }

    public ListNode reverse(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode position = head.next;
        ListNode pre = head;
        do {
            ListNode next = position.next;
            position.next = pre;
            pre = position;
            position = next;
        } while (position != null);
        head.next = null;
        return pre;
    }

    public ListNode create() {
        ListNode ln = new ListNode(1);
        ln.next = new ListNode(2);
        ln.next.next = new ListNode(3);
        ln.next.next.next = new ListNode(4);
        ln.next.next.next.next = new ListNode(5);
        return ln;
    }

    private static class ListNode {
        int val;

        ListNode next;

        ListNode(int x) {
            val = x;
        }

        @Override
        public String toString() {
            return val + "=>" + (next == null ? "null" : next.toString());
        }
    }
}
