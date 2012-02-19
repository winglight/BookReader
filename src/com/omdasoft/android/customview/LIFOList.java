package com.omdasoft.android.customview;

import java.util.EmptyStackException;

public class LIFOList {

	private static class Node {
        Object o;
        Node next;
    }

    private Node top = null;
    
    public boolean imEmpty() {
        return top == null;
    }

    public Object peek() {
        if (top == null)
            return null;
        return top.o;
    }

    public void push(Object o) {
        Node temp = new Node();
        temp.o = o;
        temp.next = top;
        top = temp;
    }

    public Object pop() {
        if (top == null)
            return null;
        
        Object o = top.o;
        top = top.next;
        return o;
    }
    
    public static void main(String args[]) {
    	LIFOList o = new LIFOList();
        o.push("aa");
        o.push("bb");
        System.out.println(o.pop());
        o.push("cc");
        System.out.println(o.pop());
        System.out.println(o.pop());
        
    }
} 
