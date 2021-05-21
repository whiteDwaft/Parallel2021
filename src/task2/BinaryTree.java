package task2;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class Node {
    int value;
    Node left;
    Node right;
    final AtomicBoolean locked;

    Node(int value) {
        this.value = value;
        right = null;
        left = null;
        locked = new AtomicBoolean(false);
    }

    public void lock() {
        while (true) {
            while (locked.get()) {
            }
            if (!locked.getAndSet(true)) return;
        }
    }

    public void unlock() {
        locked.set(false);
    }

    public boolean isRouting() {
        return !(left == null && right == null);
    }
}

public class BinaryTree {
    Node root;

    AtomicInteger counterAdd = new AtomicInteger(0);
    AtomicInteger counterDelete = new AtomicInteger(0);
    AtomicInteger counterContains = new AtomicInteger(0);
    AtomicInteger counter2 = new AtomicInteger(0);

    private void addRecursive(Node current, int value) {
        current.lock();
        if (value < current.value) {
            if (!current.isRouting()) {
                current.left = new Node(value);
                current.right = new Node(current.value);
//                current.unlock();
            } else {
                current.unlock();
                addRecursive(current.left, value);
            }
        } else if (value > current.value) {
            if (!current.isRouting()) {
                current.right = new Node(value);
                current.left = new Node(current.value);
//                current.unlock();
            } else {
                current.unlock();
                addRecursive(current.right, value);
            }
        }
        current.unlock();
    }

    private synchronized void addRoot(int value) {
        if (root == null) {
            root = new Node(value);
        }
    }

    public void add(int value) {
        if (root == null) {
            addRoot(value);
        } else {
            addRecursive(root, value);
            counterAdd.getAndIncrement();
        }
    }

    private boolean containsNodeRecursive(Node current, int value) {
        if (current == null) {
            return false;
        } else {
            current.lock();
        }
        if (current != null && current.value == value && !current.isRouting()) {
            current.unlock();
            return true;
        } else if (current.left != null && current.left.value == value && !current.left.isRouting()) {
            current.unlock();
            return true;
        } else if (current.right != null && current.right.value == value && !current.right.isRouting()) {
            current.unlock();
            return true;
        } else if (current.left != null && current.left.left != null && current.left.left.value == value && !current.left.left.isRouting()) {
            current.unlock();
            return true;
        } else if (current.left != null && current.left.right != null && current.left.right.value == value && !current.left.right.isRouting()) {
            current.unlock();
            return true;
        } else if (current.right != null && current.right.left != null && current.right.left.value == value && !current.right.left.isRouting()) {
            current.unlock();
            return true;
        } else if (current.right != null && current.right.right != null && current.right.right.value == value && !current.right.right.isRouting()) {
            current.unlock();
            return true;
        } else if (current.value < value) {
            current.unlock();
            return containsNodeRecursive(current.right, value);
        } else if (current.value > value) {
            current.unlock();
            return containsNodeRecursive(current.left, value);
        }
        current.unlock();
        return false;
    }

    public boolean containsNode(int value) {
        counterContains.getAndIncrement();
        return containsNodeRecursive(root, value);
    }

    private void deleteRecursive(Node current, int value) {
        if (current == null) {
            return;
        } else {
            current.lock();
        }
        if (current != null && current.value == value && !current.isRouting()) {
            root = null;
        } else if (current.left != null && current.left.value == value && !current.left.isRouting()) {
            root = current.right;
            current.left = null;
        } else if (current.right != null && current.right.value == value && !current.right.isRouting()) {
            root = current.left;
            current.right = null;
        } else if (current.left != null && current.left.left != null && current.left.left.value == value && !current.left.left.isRouting()) {
            current.left.left = null;
            current.left = current.left.right;
        } else if (current.left != null && current.left.right != null && current.left.right.value == value && !current.left.right.isRouting()) {
            current.left.right = null;
            current.left = current.left.left;
        } else if (current.right != null && current.right.left != null && current.right.left.value == value && !current.right.left.isRouting()) {
            current.right.left = null;
            current.right = current.right.right;
        } else if (current.right != null && current.right.right != null && current.right.right.value == value && !current.right.right.isRouting()) {
            current.right.right = null;
            current.right = current.right.left;
            current.unlock();
        } else if (current.value < value) {
            current.unlock();
            deleteRecursive(current.right, value);
        } else if (current.value > value) {
            current.unlock();
            deleteRecursive(current.left, value);
        }
        current.unlock();
    }

    public void delete(int value) {
        deleteRecursive(root, value);
        counterDelete.getAndIncrement();
    }

    private synchronized void show(Node current) {
        if (current != null) {
            show(current.left);
            if (!current.isRouting()) {
                counter2.getAndIncrement();
            }
            show(current.right);
        }
    }

    public static void main(String[] args) {
        long start;
        final int numberOfCore = 4;
        int x = 50;
        BinaryTree bt = new BinaryTree();

        for (int i = 0; i < 50000; i++) {
            bt.add(new Random().nextInt(100000));
        }

        start = Calendar.getInstance().getTimeInMillis();
        Thread[] processors = new Thread[numberOfCore];
        for (int i = 0; i < numberOfCore; i++) {
            processors[i] = new Thread(() -> {
                while (Calendar.getInstance().getTimeInMillis() - start < 5000) {
                    int key = new Random().nextInt(100000);
                    int p = new Random().nextInt(3*x);
                    if (p < x) {
                        bt.add(key);
                    } else if (p < 2*x)
                        bt.delete(key);
                    else {
                        bt.containsNode(key);
                    }
                }
            });
            processors[i].start();
        }

        for (int i = 0; i < numberOfCore; i++) {
            try {
                processors[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(bt.counterAdd.get());
        System.out.println(bt.counterDelete.get());
        System.out.println(bt.counterContains.get());
        System.out.println(bt.counterContains.get() + bt.counterDelete.get() + bt.counterAdd.get());


        bt.show(bt.root);
        System.out.println(bt.counter2);
    }
}
