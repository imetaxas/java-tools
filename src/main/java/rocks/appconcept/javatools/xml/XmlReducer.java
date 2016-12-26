package rocks.appconcept.javatools.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Stack;


public class XmlReducer {

    public static final Object SKIP_THIS_SUBTREE = new Object();

    private HashMap<String, HandlerMethod> redMethodCache = new HashMap<>();
    private HashMap<String, HandlerMethod> preMethodCache = new HashMap<>();

    private Object reduce(XmlReducerNode node, boolean preReduce) throws Exception {
        HashMap methodCache = preReduce ? preMethodCache : redMethodCache;
        HandlerMethod cm = (HandlerMethod) methodCache.get(node.name);
        if (cm == null) {
            return node;
        }

        try {
            return cm.method.invoke(cm.handler, node);
        } catch (InvocationTargetException e) {
            Object ret = cm.handler.handleException(e.getCause(), node);
            if (ret != null) {
                return ret;
            } else {
                throw e;
            }
        }
    }

    public Object build(Reader reader, ReductionHandler ... handlers) throws Exception {

        XMLStreamReader xpp = XMLInputFactory.newInstance().createXMLStreamReader(reader);

        for (ReductionHandler handler : handlers) {
            handler.setup();
        }

        // early caching instead of late -> better performance
        cacheHandlerMethods(handlers);

        Stack<XmlReducerNode> stack = new Stack<>();
        XmlReducerNode top = stack.push(new XmlReducerNode(""));
        top.path = "";
        int eventType = xpp.getEventType();
        boolean justStarted = false;
        do {
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    XmlReducerNode node = new XmlReducerNode(xpp.getLocalName());
                    node.path = top.path + "/" + node.name;
                    top = stack.push(node);
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        //noinspection unchecked
                        top.getAttributes().put(xpp.getAttributeName(i).toString(), xpp.getAttributeValue(i));
                    }
                    node.preRedux = reduce(node, true);
                    if (node.preRedux == SKIP_THIS_SUBTREE) {
                        eatElement(xpp);
                        stack.pop();
                        top = stack.peek();
                        break;
                    }
                    justStarted = true;
                    break;
                case XMLStreamReader.END_ELEMENT:
                    XmlReducerNode child = top;
                    stack.pop();
                    top = stack.peek();
                    Object reduction = reduce(child, false);
                    if (reduction != null) {
                        //noinspection unchecked
                        top.getChildren().add(reduction);
                    }
                    if (justStarted) {
                        top.text = "";
                        justStarted = false;
                    }
                    break;
                case XMLStreamReader.CHARACTERS:
                    if (!justStarted && xpp.isWhiteSpace()) {
                        break;
                    }
                    justStarted = false;
                    //char characters[] = xpp.getTextCharacters();
                    //top.text = new String(characters);
                    if (top.text == null) {
                        top.text = xpp.getText();
                    } else {
                        top.text += xpp.getText();
                    }

                    break;
                default:
                    break;
            }
            eventType = xpp.next();
        } while (eventType != XMLStreamReader.END_DOCUMENT);

        for (int i = handlers.length - 1; i >= 0; i--) {
            handlers[i].tearDown();
        }

        return top.firstChild();
    }

    private void eatElement(XMLStreamReader parser) throws XMLStreamException {
        int level = 1;
        while (level > 0) {
            int event = parser.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                level++;
            }
            if (event == XMLStreamReader.END_ELEMENT) {
                level--;
            }
        }
    }


    private void cacheHandlerMethods(ReductionHandler[] handlers) {
        for (int i = handlers.length - 1; i >= 0; i--) {
            ReductionHandler reductionHandler = handlers[i];
            Method[] methods = reductionHandler.getClass().getMethods();
            for (Method method : methods) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && params[0].equals(XmlReducerNode.class)) {
                    String name = method.getName();
                    if (name.startsWith("reduce")) {
                        HandlerMethod value = new HandlerMethod(reductionHandler, method);
                        redMethodCache.put(tagname(name.substring(6)), value);
                        redMethodCache.put(upCase(tagname(name.substring(6))), value);
                    } else if (name.startsWith("preReduce")) {
                        preMethodCache.put(tagname(name.substring(9)), new HandlerMethod(reductionHandler, method));
                    }
                }
            }
        }
    }

    private String upCase(String tagname) {
        return Character.toUpperCase(tagname.charAt(0)) + tagname.substring(1);
    }

    private String tagname(String methodName) {
        StringBuilder buf = new StringBuilder(methodName.length());
        buf.append(Character.toLowerCase(methodName.charAt(0)));
        int len = methodName.length();
        for (int i = 1; i < len; i++) {
            if (Character.isUpperCase(methodName.charAt(i))) {
                buf.append('-');
                buf.append(Character.toLowerCase(methodName.charAt(i)));
            } else {
                buf.append(methodName.charAt(i));
            }
        }
        return buf.toString();
    }

    private static class HandlerMethod {
        ReductionHandler handler;
        Method method;

        public HandlerMethod(ReductionHandler handler, Method method) {
            this.handler = handler;
            this.method = method;
        }
    }
}
