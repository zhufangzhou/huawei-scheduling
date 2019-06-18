package util;

import java.util.*;

public class DisjointSets<T> {

    private Map<T, Element> forest;
    private Set<Element> roots;

    public Map<T, Element> getForest() {
        return forest;
    }

    public Set<Element> getRoots() {
        return roots;
    }

    public DisjointSets(Collection<T> objects) {
        forest = new HashMap<>();
        roots = new HashSet<>();

        for (T object : objects) {
            Element element = new Element(object);
            forest.put(object, element);
            roots.add(element);
        }
    }

    public Element find(Element element) {
        if (element.parent.equals(element))
            return element;

        return find(element.parent);
    }

    public void union(Element ele1, Element ele2) {
        Element root1 = find(ele1);
        Element root2 = find(ele2);

        if (root1.equals(root2))
            return;

        if (root1.depth < root2.depth) {
            root1.setParent(root2);
            roots.remove(root1);
        } else {
            root2.setParent(root1);
            roots.remove(root2);

            if (root1.depth == root2.depth) {
                root1.setDepth(root1.depth+1);
            }
        }
    }

    public void union(T obj1, T obj2) {
        Element ele1 = forest.get(obj1);
        Element ele2 = forest.get(obj2);

        union(ele1, ele2);
    }

    /**
     * Whether the two objects are disjoint (belong to different subtree) or not
     * @param obj1 the object 1.
     * @param obj2 the object 2.
     * @return true if the two objects are disjoint, and false otherwise.
     */
    public boolean disjoint(T obj1, T obj2) {
        Element ele1 = forest.get(obj1);
        Element ele2 = forest.get(obj2);

        return !find(ele1).equals(find(ele2));
    }

    public class Element {
        private T object;
        private Element parent;
        private int depth;

        public Element(T object) {
            this.object = object;
            parent = this;
            depth = 0;
        }

        public T getObject() {
            return object;
        }

        public void setObject(T object) {
            this.object = object;
        }

        public Element getParent() {
            return parent;
        }

        public void setParent(Element parent) {
            this.parent = parent;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }
    }
}
