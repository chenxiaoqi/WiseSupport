package com.wisesupport.learn.lc.recursion;

import org.junit.Test;

public class Lst {

    public Lst() {
    }

    @Test
    public void test() {
        System.out.println(reverseList(create()));
        System.out.println(reverse(create()));
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
            }else{
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
