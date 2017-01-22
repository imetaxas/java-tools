package rocks.appconcept.javatools.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class XmlReducerNode {

  String name;
  String text;
  ArrayList children;
  HashMap<String, String> attributes;
  Object preRedux;
  String path;

  public XmlReducerNode(String name) {
    this.name = name;
  }

  public XmlReducerNode(String name, String text) {
    this.name = name;
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public String getText(String name) {
    if (children == null) {
      return null;
    }
    for (Iterator iter = children.iterator(); iter.hasNext(); ) {
      Object element = iter.next();
      if (element instanceof XmlReducerNode) {
        XmlReducerNode node = (XmlReducerNode) element;
        if (node.name.equals(name)) {
          return node.text;
        }
      }
    }
    return null;
  }

  public <T> ArrayList<T> getList(String name) {
    if (children == null) {
      return null;
    }
    for (Iterator iter = children.iterator(); iter.hasNext(); ) {
      Object element = iter.next();
      if (element instanceof XmlReducerNode) {
        XmlReducerNode node = (XmlReducerNode) element;
        if (node.name.equals(name)) {
          return node.children;
        }
      }
    }
    return null;
  }

  public <T> T getReduced(Class<? extends T> type) {
    if (children == null) {
      return null;
    }
    for (Iterator iter = children.iterator(); iter.hasNext(); ) {
      Object element = iter.next();
      if (element != null && type.isAssignableFrom(element.getClass())) {
        return (T) element;
      }
    }
    return null;
  }

  public String getAttribute(String attr) {
    if (attributes == null) {
      return null;
    }
    final Object attrNode = attributes.get(attr);
    if (attrNode != null) {
      return attrNode.toString();
    } else {
      return null;
    }
  }

  public Object firstChild() {
    return getChild(0);
  }

  public Object getChild(int i) {
    if (children == null) {
      return null;
    }
    return children.get(i);
  }

  public String getName() {
    return name;
  }

  public String toString() {
    return name;
  }

  public Object getPreRedux() {
    return preRedux;
  }

  public HashMap<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<String, String>();
    }
    return attributes;
  }

  @SuppressWarnings({"unchecked"})
  public <T> List<T> getChildren() {
    if (children == null) {
      children = new ArrayList<T>();
    }
    return children;
  }

  public void setText(String text) {
    this.text = text;
  }

  public XmlReducerNode getNamedChild(String string) {
    if (children == null) {
      return null;
    }
    for (Iterator iter = children.iterator(); iter.hasNext(); ) {
      Object element = iter.next();
      if (element instanceof XmlReducerNode) {
        XmlReducerNode node = (XmlReducerNode) element;
        if (node.name.equals(string)) {
          return node;
        }
      }
    }
    return null;
  }

  public String getPath() {
    return path;
  }
}
